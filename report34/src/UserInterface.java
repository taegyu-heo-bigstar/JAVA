import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.regex.Pattern;

public class UserInterface {

    private static final String ACC_REGEX = "^\\d{4}-\\d{4}$";
    private static final String OWNER_REGEX = "^[가-힣a-zA-Z]+$";

    private final Map<String, AccountManager.Account> accounts;

    private JFrame frame;

    public UserInterface(Map<String, AccountManager.Account> accounts) {
        this.accounts = (accounts != null) ? accounts : AccountManager.loadAllAccounts();
    }

    public void open() {
        frame = new JFrame("Account Manager");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(720, 420));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("계좌 생성", buildCreatePanel());
        tabs.addTab("입금", buildDepositPanel());
        tabs.addTab("출금", buildWithdrawPanel());
        tabs.addTab("이체", buildTransferPanel());
        tabs.addTab("잔액 조회", buildBalancePanel());

        frame.setContentPane(tabs);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildCreatePanel() {
        JTextField accField = new JTextField(20);
        JTextField ownerField = new JTextField(20);
        JTextField balanceField = new JTextField(20);
        JPasswordField pwField = new JPasswordField(20);

        JButton createBtn = new JButton("생성");
        createBtn.addActionListener(e -> {
            String acc = accField.getText().trim();
            String owner = ownerField.getText().trim();
            String balText = balanceField.getText().trim();
            String pw = new String(pwField.getPassword());

            if (!Pattern.matches(ACC_REGEX, acc)) {
                showError("계좌번호 형식이 올바르지 않습니다. 예: 1234-1234");
                return;
            }
            if (accounts.containsKey(acc)) {
                showError("이미 존재하는 계좌번호입니다.");
                return;
            }
            if (!Pattern.matches(OWNER_REGEX, owner)) {
                showError("소유자 이름은 한글 또는 영문만 가능합니다.");
                return;
            }
            Integer balance = parseNonNegativeInt(balText, "잔액");
            if (balance == null) return;
            if (pw.length() < 4) {
                showError("비밀번호는 최소 4자리 이상이어야 합니다.");
                return;
            }

            if (AccountManager.createAccount(accounts, acc, owner, balance, pw)) {
                showInfo("계좌가 성공적으로 생성되었습니다.");
                accField.setText("");
                ownerField.setText("");
                balanceField.setText("");
                pwField.setText("");
            } else {
                showError("계좌 생성에 실패했습니다. (중복/입력 오류/파일 저장 오류)");
            }
        });

        return formPanel(
                new String[]{"계좌번호(1234-1234)", "소유자", "초기 잔액(0 이상)", "비밀번호(4자리 이상)"},
                new JComponent[]{accField, ownerField, balanceField, pwField},
                createBtn
        );
    }

    private JPanel buildDepositPanel() {
        JTextField accField = new JTextField(20);
        JTextField amountField = new JTextField(20);

        JButton depositBtn = new JButton("입금");
        depositBtn.addActionListener(e -> {
            String acc = accField.getText().trim();
            Integer amount = parsePositiveInt(amountField.getText().trim(), "입금액");
            if (amount == null) return;

            AccountManager.Account account = accounts.get(acc);
            if (account == null) {
                showError("존재하지 않는 계좌번호입니다.");
                return;
            }
            boolean ok = AccountManager.deposit(accounts, account, amount);
            if (ok) {
                accField.setText("");
                amountField.setText("");
            }
        });

        return formPanel(
                new String[]{"계좌번호", "입금액(1 이상)"},
                new JComponent[]{accField, amountField},
                depositBtn
        );
    }

    private JPanel buildWithdrawPanel() {
        JTextField accField = new JTextField(20);
        JTextField amountField = new JTextField(20);
        JPasswordField pwField = new JPasswordField(20);

        JButton withdrawBtn = new JButton("출금");
        withdrawBtn.addActionListener(e -> {
            String acc = accField.getText().trim();
            Integer amount = parsePositiveInt(amountField.getText().trim(), "출금액");
            if (amount == null) return;
            String pw = new String(pwField.getPassword());

            AccountManager.Account account = accounts.get(acc);
            if (account == null) {
                showError("존재하지 않는 계좌번호입니다.");
                return;
            }
            boolean ok = AccountManager.withdraw(accounts, account, amount, pw);
            if (ok) {
                accField.setText("");
                amountField.setText("");
                pwField.setText("");
            }
        });

        return formPanel(
                new String[]{"계좌번호", "출금액(1 이상)", "비밀번호"},
                new JComponent[]{accField, amountField, pwField},
                withdrawBtn
        );
    }

    private JPanel buildTransferPanel() {
        JTextField fromField = new JTextField(20);
        JTextField toField = new JTextField(20);
        JTextField amountField = new JTextField(20);
        JPasswordField pwField = new JPasswordField(20);

        JButton transferBtn = new JButton("이체");
        transferBtn.addActionListener(e -> {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            Integer amount = parsePositiveInt(amountField.getText().trim(), "이체액");
            if (amount == null) return;
            String pw = new String(pwField.getPassword());

            AccountManager.Account fromAcc = accounts.get(from);
            if (fromAcc == null) {
                showError("출금 계좌번호가 존재하지 않습니다.");
                return;
            }
            AccountManager.Account toAcc = accounts.get(to);
            if (toAcc == null) {
                showError("입금 계좌번호가 존재하지 않습니다.");
                return;
            }
            if (fromAcc == toAcc) {
                showError("동일 계좌로는 이체할 수 없습니다.");
                return;
            }
            boolean ok = AccountManager.transfer(accounts, fromAcc, toAcc, amount, pw);
            if (ok) {
                fromField.setText("");
                toField.setText("");
                amountField.setText("");
                pwField.setText("");
            }
        });

        return formPanel(
                new String[]{"출금 계좌번호", "입금 계좌번호", "이체액(1 이상)", "출금 계좌 비밀번호"},
                new JComponent[]{fromField, toField, amountField, pwField},
                transferBtn
        );
    }

    private JPanel buildBalancePanel() {
        JTextField accField = new JTextField(20);
        JPasswordField pwField = new JPasswordField(20);

        JButton showBtn = new JButton("조회");
        showBtn.addActionListener(e -> {
            String acc = accField.getText().trim();
            String pw = new String(pwField.getPassword());

            AccountManager.Account account = accounts.get(acc);
            if (account == null) {
                showError("존재하지 않는 계좌번호입니다.");
                return;
            }
            if (!account.verifyPassword(pw)) {
                showError("비밀번호가 올바르지 않습니다.");
                return;
            }
            showInfo(account.getOwner() + "님의 잔액: " + account.getBal());
        });

        return formPanel(
                new String[]{"계좌번호", "비밀번호"},
                new JComponent[]{accField, pwField},
                showBtn
        );
    }

    private JPanel formPanel(String[] labels, JComponent[] fields, JButton actionButton) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.0;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(fields[i], gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(actionButton, gbc);

        return panel;
    }

    private Integer parseNonNegativeInt(String text, String label) {
        if (text == null || text.isBlank()) {
            showError(label + "을(를) 입력하세요.");
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                showError(label + "은(는) 0 이상이어야 합니다.");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            showError(label + "은(는) 숫자여야 합니다.");
            return null;
        }
    }

    private Integer parsePositiveInt(String text, String label) {
        if (text == null || text.isBlank()) {
            showError(label + "을(를) 입력하세요.");
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                showError(label + "은(는) 1 이상이어야 합니다.");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            showError(label + "은(는) 숫자여야 합니다.");
            return null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "안내", JOptionPane.INFORMATION_MESSAGE);
    }
}