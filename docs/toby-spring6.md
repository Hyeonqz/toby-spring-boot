# 토비의 스프링6 - 이해와 원리

기본적으로 객체에 toString 이 존재하지 않는다면, 원하는 값이 나오지 않을 것이다 <br>
모든 객체의 상위인 Object 에 구현되어 있는대로 나올 것이다 <br>
> 클래스이름@메모리주소 ex) Payment@1few6y

위와 같은 값이 출력이 될 것이다 <br>
toString() 으로 값 출력이 필요하다면 원하는 값으로 재정의 하는 방법을 사용해야 한다 <br>

```java
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 값은 무시한다.
public record ExRateData(
	String result,
	Map<String, BigDecimal> rates
) {

}
```

#### JSON 데이터 처리 방법
```java
		URL url = new URL("https://open.er-api.com/v6/latest/" + currency);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

		String response = br.lines().collect(Collectors.joining());

		ObjectMapper mapper = new ObjectMapper();
		ExRateData exRateData = mapper.readValue(response, ExRateData.class);

		BigDecimal krw = exRateData.rates().get("KRW");
		String result = exRateData.result();
```


자바의 최상위 객체는 오브젝트이다 <br>
다른 의미로는 인스턴스 라고 도 할 수 있다 <br>
자바에서는 배열도 오브젝트 이다 <br>

클래스레벨의 의존관계와 런타임레벨의 의존관계가 다를 수 있다 <br>
이게 바로 스프링이 제공해주는 핵심 원리이다 <br>

의존관계는 클래스간의 의존, 호출 의존, 사용 의존등이 존재한다 <br>

#### 관심사의 분리
- 대표적인 리팩토링 --> 메소드 추출

#### 상속을 통한 확장
바뀔 수 있는 코드들은 제거를 한 후에 상속을 받아서 사용을 한다 <br>
```java
public abstract class PaymentService {
	// 재사용성 높은 코드
	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
		BigDecimal krw = this.getExchangeRate(currency);
		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(krw);
		LocalDateTime validUntil = LocalDateTime.now().plusMinutes(30);

		return new Payment(orderId, currency, foreignCurrencyAmount, krw, convertedAmount, validUntil);
	}

	// 요구사항에 따라 바뀔 코드
	abstract BigDecimal getExchangeRate(String currency) throws IOException;
}

public class WebApiExRatePaymentService extends PaymentService{

	@Override
	public BigDecimal getExchangeRate (String currency) throws IOException {
		URL url = new URL("https://open.er-api.com/v6/latest/" + currency);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		String response = br.lines().collect(Collectors.joining());
		br.close();

		ObjectMapper mapper = new ObjectMapper();
		ExRateData exRateData = mapper.readValue(response, ExRateData.class);

		BigDecimal krw = exRateData.rates().get("KRW");
		String result = exRateData.result();
		return krw;
	}

}

public class SimpleExRatePaymentService extends PaymentService{
	private static final String USD = "USD";

	@Override
	BigDecimal getExchangeRate (String currency) throws IOException {
		if(currency.equals(USD))
			return BigDecimal.valueOf(1000);

		throw new IllegalArgumentException("지원되지 않는 통화 입니다.");
	}

}

```

#### 클래스의 분리
기존의 상속 구조로 코드를 짯을 때 만약 상위 구조가 변경이 된다면 하위 구조들 또한 변경이 일어나야 한다 <br>
```java
	private final WebApiExRateProvider exRateProvider;

	public PaymentService () {
		this.exRateProvider = new WebApiExRateProvider();
	}
```

WebApiExRateProvider 가 변경이되면, 생성자 및 인스턴스 또한 수정이 되어야 한다 <br>

위 문제를 해결하기 위해서는 상속이 아닌 '인터페이스' 를 활용해야 한다 <br>
```java
public interface ExRateProvider {
	// 인터페이스는 기본적으로 메소드가 public 임

	BigDecimal getExRate(String currency) throws IOException;

}

public class PaymentService {
	// 한번만 만들어두고 재사용을 한다.
	private final ExRateProvider exRateProvider;

	public PaymentService () {
		this.exRateProvider = new SimpleExRateProvider();
	}

	// 재사용성 높은 코드
	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
		BigDecimal exRate = exRateProvider.getExRate(currency);
		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);
		LocalDateTime validUntil = LocalDateTime.now().plusMinutes(30);

		return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntil);
	}

}
```

#### 관계 설정 책임의 분리
런타임에 의존해야 할 코드 레벨에서 설정을 해야한다 <br>
```java
	public static void main (String[] args) throws IOException {
		PaymentService paymentService = new PaymentService(new SimpleExRateProvider());
		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());
	}

public class PaymentService {
	// 한번만 만들어두고 재사용을 한다.
	private final ExRateProvider exRateProvider;

	// 의존관계 설정 책임 -> 위 책임을 PaymentService 가 가지고 있음.
	public PaymentService (ExRateProvider exRateProvider) {
		this.exRateProvider = exRateProvider;
	}
}

```

#### 오브젝트 팩토리
```java

public class ObjectFactory {
	public PaymentService paymentService () {
		return new PaymentService(new SimpleExRateProvider());
	}

}

public static void main (String[] args) throws IOException {
	ObjectFactory objectFactory = new ObjectFactory();
	PaymentService paymentService = objectFactory.paymentService();

	Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
	System.out.println(payment.toString());
}

```

#### 개방 폐쇄 원칙(OCP)
클래스나 모듈은 확장에는 열려 있어야 하고 변경에는 닫혀 있어야 한다 <br>
- 높은 응집도와 낮은 결합도 (High Coherence and low coupling)
  - 응집도가 높다는 것은 하나의 모듈이 하나의 책임 또는 관심사에 집중되어있다는 뜻
  - 변화가 일어날 때 해당 모듈에서 변하는 부분이 크다.
  - 책임과 관심사가 다른 모듈과는 낮은 결합도, 즉 느슨하게 연결된 형태를 유지하는 것이 바람직하다
- 전략 패턴(Strategy Pattern)
  - 필요에 따라 변경이 필요한 알고리즘(=기능 수행 절차)를 인터페이스를 통해 통째로 외부로 분리시킴
```java
// 전략패턴 예시
public class Sort {
	public static void main (String[] args) {
		List<Integer> scores = Arrays.asList(1,56,562,62,2,5,7);
		// Collection.sort 는 전략 패턴으로 구현되어 있음.
		Collections.sort(scores);

		List<String> strings = Arrays.asList("Java","MySQL","asp","Spring");
		Collections.sort(strings, (o1, o2) -> o1.length() - o2.length());

		scores.forEach(System.out::println);
		strings.forEach(System.out::println);
	}
}
```

- 제어의 역전 (프레임워크의 기본 동작 원리)

#### 스프링 컨테이너와 의존관계 주입
BeanFactory(=스프링 컨테이너) -> 스프링 IoC/DI 컨테이너 <br>
상태 정보를 넣지 않은 빈들은 여러 스레드에서 1개의 인스턴스를 동시에 사용해도 괜찮다 <br>
오브젝트 1개를 만들어서 여러 사용자가 동시에 이용해도 된다 <br>

즉 스프링 어플리케이션이 기동될 때 Bean 들을 오브젝트에 담아두고 필요할 때 꺼내서 사용을 한다 <br>
만들어진 오브젝트 사이의 관계 설정까지 해주는 -> DI 또한 일어난다 <br>

- 구성 정보를 가져오는 방법
  - @Component
  - @Service
  - @Repository

어떤 코드들은 @ComponentScan 을 이용하고 어떤 코드는 직접 @Bean 을 붙여서 오브젝트를 생성해야 할까? <br>

- 싱글톤 레지스트리(Singleton Registry)
  - 1개의 오브젝트를 만들어두고 공유하여 사용한다.

스프링은 대부분 서버 어플리케이션이기 때문에 사용자 요청마다 매번 오브젝트를 생성하면 비용 낭비가 심하다 <br>
그러므로 하나를 만들어서 사용자들에게 공유를 해야한다 <br>

스프링 내부에서 만들어지는 빈 오브젝트는 딱 1개만 만들어진다 <br>
사용하는 디른 오브젝트가 여러개여도 동일한 오브젝트가 사용이 된다 <br>

#### DI 와 디자인 패턴
- 패턴을 목적에 따라 분류, 패턴을 스코프에 따른 분류
  - 클래스 패턴: 상속, Object 패턴: 합성

오브젝트 합성을 이용하는 디자인 패턴을 적용할 때 -> 스프링의 의존관계 주입(=DI)을 사용 <br>

환율 정보가 필요할 때 매번 Web API 를 호출해야 할까? <br>
-> 환율 정보 캐시의 도입 <br>

-> 데코레이터 디자인 패턴을 활용한다 <br>

오브젝트에 부가적인 기능/책임을 동적으로 부여한다 <br>

#### 의존성 역전 원칙(DIP)
의존성 주입과는 다르다는것 을 알아야 한다 <br>

- 상위 수준의 모듈은 하위 수준의 모듈에 의존해서는 안 된다. -> 둘 모두 추상화에 의존해야 한다.
- 추상화는 구체적인 사항에 의존해서는 안 된다. -> 구체적인 사항은 추상화에 의존해야 한다.

상위 수준의 모듈? 하위 수준의 모듈? 이 뭘까 <br>
자바에서는 패키지로 잘 구분해두면 jar 파일로 분리할 수 있다 <br>

추상화에 의존한다? -> 인터페이스를 도입한다 <br>

### 테스트
#### 수동 테스트의 한계
- 프린트된 메시지를 수동으로 확인하는 방법은 불편하다
- 사용자 웹 UI 까지 개발한 뒤에 확인하는 방법은 테스트가 실패했을 때 확인할 코드가 많다 
- 테스트할 대상이 많아질 수록 검증하는데 시간이 많이 걸리고 부정확함

-> 작은 크기의 자동 수행되는 테스트 가 필요하다(=개발자가 만드는 테스트) <br>
- 개발한 코드에 대한 검증 기능을 코드로 작성한다.
- 자동으로 테스트를 수행하고 결과를 확인한다.
- 테스팅 프레임워크를 활용한다.
- 테스트 작성과 실행도 개발 과정의 일부이다.


#### JUnit5
- @Test 테스트 메소드
- @BeforeEach 테스트
  - 각 테스트 전에 실행된다.
- 테스트 마다 새로운 인스턴스가 생성된다.


List.of() -> 불변의 값을 가진 리스트 만듬 <br>
Arrays.asList() -> 가변 방식의 리스트 만듬 <br>

테스트는 다양한 측면에서 여러가지로 만들어보자 <br>
```java
	private Sort sort;

	@BeforeEach
	void beforeEach () {
		sort = new Sort();
	}

	@Test
	@DisplayName("BDD 스타일 테스트 => 길이 순으로 정렬을 한다")
	void Sort_Length_Test() {
	    // given	private Sort sort;

      @BeforeEach
      void beforeEach () {
        sort = new Sort();
      }

      @Test
      @DisplayName("BDD 스타일 테스트 => 길이 순으로 정렬을 한다")
      void Sort_Length_Test() {
        // given

        // when
        List<String> list = sort.sortByLength(Arrays.asList("aa", "b", "ccc"));

        // then
        Assertions.assertThat(list).isEqualTo(List.of("b","aa","ccc"));
      }

		// when
		List<String> list = sort.sortByLength(Arrays.asList("aa", "b", "ccc"));

	    // then
		Assertions.assertThat(list).isEqualTo(List.of("b","aa","ccc"));
	}
```

테스트는 가능하면 편하고 빠르게 작성하는 연습해야 한다 <br>
```java
	@BeforeEach
	void beforeEach () {
		sort = new Sort();
		System.out.println(this);
	}
```

테스트는 다른 테스트에 영향을 받지 않고 독립적으로 실행이 되어야 한다 <br>
그러므로 테스트 실행 순서에 영향을 받아서는 안된다 <br>

그걸 보장해주기 위해 테스트 메소드 1개당 테스트 인스턴스가 1개씩 생성이 된다 <br>
즉 테스트 메소드 1개당 매번 테스트 인스턴스가 생성이 된다 <br>
-> Junit5 기본적인 동작 방식이다 <br>

테스트 실패 이유? <br>
- 예외를 통과하지 못해서
- AssertThat 검증을 실패해서

우리가 제어할 수 없는 외부시스템에 문제가 생기면? <br>

#### 테스트의 구성 요소
stub 을 활용하여 외부 호출을 테스트 한다 -> 테스트 협력자를 사용한다. <br>

#### 테스트와 DI
Stub = Test Double(대역) 을 알아보자 <br>

BigDecimal 은 Assertions.assertThat() 의 isEqualByComparingTo() 메소드를 활용한다 <br>
isEqualTo() 는 소수점 2자리 까지 비교 isEqualByComparingTo() 는 소수점 6자리 까지 비교 <br>

- 수동 DI 를 이용하는 테스트
```java
public class ExRateProviderStub implements ExRateProvider{
	private BigDecimal exRate;

	public ExRateProviderStub (BigDecimal exRate) {
		this.exRate = exRate;
	}

	public BigDecimal getExRate () {
		return exRate;
	}

	public void setExRate (BigDecimal exRate) {
		this.exRate = exRate;
	}

	@Override
	public BigDecimal getExRate (String currency) throws IOException {
		return exRate;
	}

}

@Test
@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
void converted_Amount() throws IOException {
  // given
  this.getPayment(valueOf(500), valueOf(5_000));
  this.getPayment(valueOf(1_000), valueOf(10_000));
  this.getPayment(valueOf(30_000), valueOf(300_000));
}

@NonNull
private void getPayment (BigDecimal exRate, BigDecimal convertedAmount) throws IOException {
  PaymentService paymentService = new PaymentService(new ExRateProviderStub(exRate));

  // when
  Payment payment = paymentService.prepare(1L, "USD", TEN);

  // then
  assertThat(payment.getExRate()).isEqualByComparingTo(exRate);
  assertThat(payment.getConvertedAmount()).isEqualTo(convertedAmount);
  assertThat(payment.getCurrency()).isEqualTo("USD");
}
```

위 코드처럼 활용 <br>

테스트용 협렵자/의존 오브젝트를 테스트 대상에 직접 주입하고 테스트 <br>
자동화 하기 위해서는 스프링 구성정보를 이용해서 지정하고 컨테이너로부터 테스트 대상을 가져와서 테스트 한다
- @ContextConfiguration
- @Autowired

```java
// 변경전
class PaymentServiceTest {

	@Test
	@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
	void converted_Amount () throws IOException {
		// given
		BeanFactory beanFactory = new AnnotationConfigApplicationContext(TestObjectFactory.class);
		PaymentService paymentService = beanFactory.getBean(PaymentService.class);

		this.getPayment(valueOf(500), valueOf(5_000));
		this.getPayment(valueOf(1_000), valueOf(10_000));
		this.getPayment(valueOf(30_000), valueOf(300_000));
	}

}

// 변경 후 -> 스프링 구성정보를 이용한다.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestObjectFactory.class) // 구성정보 클래스를 지정한다.
class PaymentServiceTest {

	@Autowired
	private BeanFactory beanFactory;

	@Test
	@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
	void converted_Amount () throws IOException {
		// given
		beanFactory.getBean(PaymentService.class);

		this.getPayment(valueOf(500), valueOf(5_000));
		this.getPayment(valueOf(1_000), valueOf(10_000));
		this.getPayment(valueOf(30_000), valueOf(300_000));
	}

}

// 컨테이너 내부에 있는 Bean 을 바로 가져온다.
class PaymentServiceTest {

	@Autowired
	private PaymentService paymentService;

	@Test
	@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
	void converted_Amount () throws IOException {
		// given

		this.getPayment(valueOf(500), valueOf(5_000));
		this.getPayment(valueOf(1_000), valueOf(10_000));
		this.getPayment(valueOf(30_000), valueOf(300_000));
	}

}
```

### 학습 테스트 (Learning Test)
직접 만들지 않은 코드, 라이브러리, 레거시 시스템에 대한 테스트 <br>
테스트 대상의 사용방법을 익히고 동작방식을 확인하는데 유용하다 <br>

라이브러리의 기능을 테스트 하기 위한 가이드를 제공할 때 유용하게 사용한다 <br>
나는 알지만 팀원은 모를 수 있으니 가이드로 사용하기 좋다 <br>

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPaymentConfig.class)
public class PaymentServiceSpringTest {

	@Autowired PaymentService paymentService;
	@Autowired Clock clock;

	@Test
	void validUntil() throws IOException {
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		// valid until 이 prepare() 30분 뒤로 설정됐는가?
		LocalDateTime now = LocalDateTime.now(this.clock);
		LocalDateTime expectedValidUntil = now.plusMinutes(30);

		Assertions.assertThat(payment.getValidUntil()).isEqualTo(expectedValidUntil);
	}

	@NonNull
	private void getPayment (BigDecimal exRate, BigDecimal convertedAmount, Clock clock) throws IOException {
		PaymentService paymentService = new PaymentService(new ExRateProviderStub(exRate), clock);

		// when
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		// then
		assertThat(payment.getExRate()).isEqualByComparingTo(exRate);
		assertThat(payment.getConvertedAmount()).isEqualTo(convertedAmount);
		assertThat(payment.getCurrency()).isEqualTo("USD");
	}

}

```

SpringBoot 테스트를 사용하면은 Spring 컨테이너와 구성정보를 가져와서 검증만 하면 된다 <br>
훨씬 간단하게 사용할 수 있다 <br>

#### 도메인 오브젝트 테스트
테스트의 꽃이라고 불리는 도메인 오브젝트 테스트를 알아보자 <br>
도메인 모델 아키텍처 패턴 -> 도메인 로직, 비즌니스 로직을 어디에 둘 지를 결정하는 패턴 <br>
1. 트랜잭션 스크립트 -> 서비스 메소드(ex-PaymentService.prepare)
2. 도메인 모델 -> 도메인 모델 오브젝트(ex-Payment)

- 생성자보다는 -> 팩토리 메소드 패턴을 이용하자

```java
// 팩토리 메소드 전 코드
	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
		BigDecimal exRate = exRateProvider.getExRate(currency);
		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);
		LocalDateTime validUntil = LocalDateTime.now(clock).plusMinutes(30);

		return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntil);
	}
	
// 팩토리 메소드 적용 후 코드
    public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
    BigDecimal exRate = exRateProvider.getExRate(currency);


    return Payment.createPrepare(orderId, currency, foreignCurrencyAmount, exRate, LocalDateTime.now());
}

// 팩토리 메소드 코드
public static Payment createPrepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount, BigDecimal exRate, LocalDateTime now) {

  BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);
  LocalDateTime validUntil = now.plusMinutes(30);

  return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntil);
}

```

## 템플릿
#### Spring 과 JDK 업그레이드
build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.3'
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    sourceCompatibility = '21'
}
```

#### 템플릿
코드 중에서 변경이 거의 일어나지 않으며 일정한 패턴으로 유지되는 특성을 가진 부분을 자유롭게 변경되는 성질을 가진 부분으로부터 독립시켜서 효과적으로 활용할 수 있도록 하는 방법 <br>

```java
  public BigDecimal getExRate (String currency) {
    // checked Exception -> unCheckedException
    String url = "https://open.er-api.com/v6/latest/" + currency;

    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    String response;
    try {
      HttpURLConnection urlConnection = (HttpURLConnection)uri.toURL().openConnection();

      // java 가 직접 close 를 해준다 -> autoClosable -> autoClosable 구현체를 구현하고 있으면 자동으로 resource 를 닫아준다
      try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
        response = br.lines().collect(Collectors.joining());
        // br.close() 위 메소드가 필요가 없어진다.
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }


    ExRateData exRateData;
    try {
      ObjectMapper mapper = new ObjectMapper();
      exRateData = mapper.readValue(response, ExRateData.class);

      return exRateData.rates().get("KRW");
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
```

위 코드는 메소드에 throws Exception 을 던졌지만, try~catch 블록을 통해서 uncheckedException 으로 만들었다 <br>
checkedException -> 컴파일 시점에 오류 체크가 됨 
- Exception 클래스 직접 상속함
- 서버를 실행하는 컴파일 시점에 확인됨
- 명시적으로 try~catch, throws 선언 필수 ex) IOException
RuntimeException -> 컴파일 시점에 오류 체크가 안됌 -> 런타임 시점에만 오류가 체킹이 됌
- 컴파일 시점에 확인 불가
- 명시적인 예외 처리가 안됨 ex) NullPointException

#### 변하는 코드 분리하기
- 메소드 추출 -> 템플릿의 탄생

템플릿? -> 어떤 목적을 위해 미리 만들어둔 모양이 있는 틀 <br>
템플릿 메소드 패턴도 템플릿을 사용한다 <br>

템플림 메소드 패턴? <br>
고정된 틀의 로직을 가진 템플릿 메소드를 슈퍼클래스에 두고, 바뀌는 부분을 서브클래스의 메소드에 두는 구조로 이뤄진다 <br>

#### 인터페이스의 도입과 클래스 분리
개방 폐쇄 원칙을 지키기 위해서 인터페이스를 도입한다 <br>

- Callback + Method Injetion
콜백은 실행되는 것을 목적으로 다른 오브젝틑의 메소드에 전달되는 오브젝트 파라미터로 전달되지만 값을 참조하기 위한 것이 아니라 특정 로직을 담은 메소드를 실행시키는 것이 목적 <br>

템플릿/콜백은 전략 패턴의 특별한 케이스이다 <br>
전략패턴에는 컨텍스트 및 바뀌는 알고리즘이 존재한다 <br>

템플릿은 전략 패턴의 컨텍스트에 해당한다 <br>
콜백은 전략 패턴의 전략에 해당한다 <br>

템플릿/콜백은 메소드 하나만 가진 전략 인터페이스를 사용하는 전략 패턴을 의미한다 <br>
-> 메소드 주입이라고 한다 <br>

의존 오브젝트가 메소드 호출 시점에 파라미터로 전달되는 방식이다 <br>
- 의존관계 주입의 한 종류이다 ex) DI 랑 같은 개념임
- 메소드 호출 주입이라고도 한다

```java
	@Override
	public BigDecimal getExRate (String currency) {
		String url =  URL + currency;
		
		// 메소드 주입은 Bean 으로 작성하는게 아닌 우리가 작성하는 로직에서 작성해야 한다.
		return this.runApiForExRate(url, new SimpleApiExecutor());
	}

	// 1. 재사용성 증가를 위해서 메소드 추출을 한다.       -> 구현체가 아닌 인터페이스를 파라미터로 사용한다.
	private BigDecimal runApiForExRate (String url, ApiExecutor apiExecutor) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String response;
		try {
			response = apiExecutor.execute(uri); // 주입 받기
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ExRateData exRateData;
		try {
			return this.extractExRate(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
```

#### 템플릿/콜백의 작업 흐름
```java
public interface ApiExecutor {
	String execute(URI uri) throws IOException;
}

@Override
public BigDecimal getExRate (String currency) {
  String url =  URL + currency;
  // 메소드 주입은 Bean 으로 작성하는게 아닌 우리가 작성하는 로직에서 작성해야 한다.

  return this.runApiForExRate(url, new SimpleApiExecutor(), new ErApiExRateExtractor());
}
```

클라이언트에서는 인터페이스의 구현체를 주입시키고, 실제 메소드에서는 인터페이스로 파라미터로 만들어서 주입을 받는다 <br>
```java
	private BigDecimal runApiForExRate (String url, ApiExecutor apiExecutor, ExRateExtractor exRateExtractor) {
		String response;
		try {
			response = apiExecutor.execute(uri); //인터페이스 메소드 호출
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ExRateData exRateData;
		try {
			return exRateExtractor.extract(response); // 인터페이스 메소드 호출
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
```
```java
// 최종 template 을 분리하고 콜백 메소드 패턴으로 만든 로직
public class WebApiExRateProvider implements ExRateProvider {
	private static final String URL = "https://open.er-api.com/v6/latest/";
	private ApiTemplate template = new ApiTemplate();

	@Override
	public BigDecimal getExRate (String currency) {
		String url = URL + currency;
		
		return template.getExRate(url, new HttpClientApiExecutor(), new ErApiExRateExtractor());
	}

}

```

#### Spring 에서 제공하는 템플릿 & 콜백 패턴 클래스
```java
// 디폴트 콜백 패턴
@Slf4j
@Component
public class WebApiExRateProvider implements ExRateProvider {
  private static final String URL = "https://open.er-api.com/v6/latest/";
  private ApiTemplate template = new ApiTemplate();

  @Override
  public BigDecimal getExRate (String currency) {
    String url = URL + currency;

    return template.getForExRate(url);
  }

}

public class ApiTemplate {
  private final ApiExecutor apiExecutor;
  private final ExRateExtractor exRateExtractor;

  public ApiTemplate () {
    this.apiExecutor = new HttpClientApiExecutor();
    this.exRateExtractor = new ErApiExRateExtractor();
  }

  public BigDecimal getForExRate(String url) {
    return this.getExRate(url, this.apiExecutor, this.exRateExtractor);
  }

  // 1. 재사용성 증가를 위해서 메소드 추출을 한다.
  public BigDecimal getExRate (String url, ApiExecutor apiExecutor, ExRateExtractor exRateExtractor) {
    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    String response;
    try {
      response = apiExecutor.execute(uri);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ExRateData exRateData;
    try {
      return exRateExtractor.extract(response);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}

```

```java
// 템플릿 콜백 메서드 -> 생성 자 사용
	public ApiTemplate () {
		this.apiExecutor = new HttpClientApiExecutor();
		this.exRateExtractor = new ErApiExRateExtractor();
	}

	public ApiTemplate (ApiExecutor apiExecutor, ExRateExtractor exRateExtractor) {
		this.apiExecutor = new HttpClientApiExecutor();
		this.exRateExtractor = new ErApiExRateExtractor();
	}
```

Spring 이 제공하는 템플릿 예시
- RestTemplate -> api 관련 template 콜백 메소드 예시로 좋음
- JdbcTemplate
- JmsTemplate   
- TransactionTemplate
- HibernateTemplate

등등 여러가지가 있다 <br>

RestTemplate 처럼 만들어보자 이제 <br>
- HTTP Client 라이브러리 확장: ClientHttpRequestFactory
- Message Body 를 변환하는 전략: HttpMessageConverter


<br>

## 예외
- 예외는 정상적인 프로그램 흐름을 방해하는 사건
- 예외적인 상황에서만 사용
- 많은 경우 예외는 프로그램 오류, 버그 때문에 발생

#### 예외가 발생하면?
- 예외 상황을 복구해서 정상적인 흐름으로 전환할 수 있는가?
  - 방법1: 재시도
  - 방법2: 다른 대안을 찾는다.
- 버그인가?
  - 우리 코드의 오류 때문에 버그가 발생할 수 있다.
  - 예외가 발생한 코드의 버그인가?
  - 클라이언트의 버그인가?
- 제어할 수 없는 예외상황인가?
  - ex) 네트워크 장애, db 장애

#### 예외를 잘못 다루는 코드
- 예외를 무시하는 코드
```java
// 1번: 예외에 아무것도 없는 코드
try {
	
} catch (Exception e) {
	
}

// 2번: 예외를 처리하지 않고 로그만 찍는 것.
try {

} catch (Exception e) {
    e.printStackTrace();
}
```

위 2가지는 완벽한 안티패턴이다. 실무에서는 위처럼 코드를 작성하면 안된다. <br>

- 무의미하고 무책임한 throws
```java
public void method1() throws Exception {
	method2();
}

public void method2() throws Exception {
  method3();
}

public void method3() throws Exception {
  // Exception 이 필요한 예외 로직
}
```

무분별하게 메소드에 던지는 Exception 은 다른 로직에 영향을 끼칠 때 불필요하게 Exception 을 던지게 된다 <br>

예외의 종류
- Error -> 시스템에 비정상적인 상황 ex) OutOfMemoryError, ThreadDeath
- Exception -> Checked: 컴파일시 에러가 잡힌다.
- RuntimeException -> Unchecked: 컴파일시 에러가 안잡힌다 -> 실행 중에 무언가 동작할 때 잡힌다.

#### Checked Exception
Exception 인터페이스를 상속해서 만들어진 Exception 중에서 RuntimeException 을 상속하지 않은 예외들을 의미한다.
- catch 나 throws 를 강요
- 예외 발생시 컴파일이 불가능함 -> 실행을 할 수 없음
- 초기 라이브러리의 잘못된 예외 설계/사용
- 예외를 복구할 수 없다면 RuntimeException 이나 적절한 추상화 레벨의 예외로 전환해서 던질 것

#### 예외의 추상화와 전환
- 사용 기술에 따라 같은 문제에 대해 다른 종류의 예외 발생
- 적절한 예외 추상화와 예외 번역이 필요






































## 서비스 추상화





