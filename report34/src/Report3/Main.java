package Report3;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Map<String, AccountManager.Account> accounts = AccountManager.loadAllAccounts();

        // 그래픽 환경이 아니면(예: headless) 즉시 종료
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("❌ 그래픽 환경을 사용할 수 없습니다(headless). 프로그램을 종료합니다.");
            System.exit(1);
        }

        // Swing 초기화/실행 중 예외가 나면 즉시 종료
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("❌ GUI 실행 중 오류가 발생했습니다. 프로그램을 종료합니다.");
            e.printStackTrace();
            System.exit(1);
        });

        try {
            SwingUtilities.invokeAndWait(() -> new UserInterface(accounts).open());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ GUI 실행이 중단되었습니다. 프로그램을 종료합니다.");
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.err.println("❌ GUI 로드에 실패했습니다. 프로그램을 종료합니다.");
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            cause.printStackTrace();
            System.exit(1);
        }
    }
}
