package diameter.transaction;

import diameter.domain.message.DiameterMessage;

public class Transaction {
    private final DiameterMessage request;
    private       DiameterMessage answer;

    public Transaction(DiameterMessage request) {
        this.request = request;
    }

    public DiameterMessage getRequest() {
        return request;
    }

    public DiameterMessage getAnswer() {
        return answer;
    }

    public void setAnswer(DiameterMessage answer) {
        this.answer = answer;
    }
}
