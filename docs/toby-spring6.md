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


















