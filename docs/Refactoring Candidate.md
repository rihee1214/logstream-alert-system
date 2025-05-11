1. ObjectMapper mapper = new ObjectMapper();
	1. 해당 요소는 Json String 생성을 목표로, 내부적으로 ObjectNode를 생성하기 위해 호출한다.
	2. mapper는 thread safety하기 때문에 private static final로 호출하고 있다.
	3. 이것을 사용하는 클래스가 많아지면 차라리 LogJsonUtils든, JsonUtils로 만들어서 node를 자동 생성하도록 하는것이 좋을 것이다.