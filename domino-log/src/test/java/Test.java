public class Test {
	public static void main(String[] args) {


	}



	public static void send (){
		//初始化服务
		LogService.OBJ.init("http://192.168.0.14:8003");

		for (int i = 0; i < 2000; i++) {
			LogService.OBJ.sendLog(0, null);
		}
	}
}
