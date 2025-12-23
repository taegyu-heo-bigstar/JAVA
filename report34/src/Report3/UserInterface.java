package Report3;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class UserInterface {

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
            String acc = accField.getText();
            String owner = ownerField.getText();
            String balText = balanceField.getText();
            String pw = new String(pwField.getPassword());

            AccountManager.Result result = AccountManager.createAccountFromUi(accounts, acc, owner, balText, pw);
            if (result.ok) {
                showInfo(result.message);
                accField.setText("");
                ownerField.setText("");
                balanceField.setText("");
                pwField.setText("");
            } else {
                showError(result.message);
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
            AccountManager.Result result = AccountManager.depositFromUi(accounts, accField.getText(), amountField.getText());
            if (result.ok) {
                showInfo(result.message);
                accField.setText("");
                amountField.setText("");
            } else {
                showError(result.message);
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
            String pw = new String(pwField.getPassword());
            AccountManager.Result result = AccountManager.withdrawFromUi(accounts, accField.getText(), amountField.getText(), pw);
            if (result.ok) {
                showInfo(result.message);
                accField.setText("");
                amountField.setText("");
                pwField.setText("");
            } else {
                showError(result.message);
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
            String pw = new String(pwField.getPassword());
            AccountManager.Result result = AccountManager.transferFromUi(
                    accounts,
                    fromField.getText(),
                    toField.getText(),
                    amountField.getText(),
                    pw
            );
            if (result.ok) {
                showInfo(result.message);
                fromField.setText("");
                toField.setText("");
                amountField.setText("");
                pwField.setText("");
            } else {
                showError(result.message);
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
            String pw = new String(pwField.getPassword());
            AccountManager.Result result = AccountManager.balanceFromUi(accounts, accField.getText(), pw);
            if (result.ok) {
                showInfo(result.message);
            } else {
                showError(result.message);
            }
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "안내", JOptionPane.INFORMATION_MESSAGE);
    }
}