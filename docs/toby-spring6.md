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
