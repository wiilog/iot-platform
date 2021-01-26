public class Main {

    public static void main(String[] args) {
        String mode = args[0];
        switch (mode) {
            case Constant.MODE_DISPATCH_CENTER:
                System.out.println("toto");
                break;
            case Constant.MODE_WORKER:
                System.out.println("tata");
                break;
            case Constant.MODE_HTTP_ENDPOINT:
                System.out.println("tutu");
                break;
            default:
                break;
        }
    }
}
