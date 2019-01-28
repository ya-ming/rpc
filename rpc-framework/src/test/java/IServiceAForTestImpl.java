public class IServiceAForTestImpl implements IServiceAForTest{
    public void methodA()
    {
        System.out.println("IServiceAForTestImpl::methodA ");
    }
    public String methodB(String input) {
        System.out.println("IServiceAForTestImpl::methodB " + input);
        return "IServiceAForTestImpl::methodB " + input;
    }
}
