package hanamuramiyu.karakuri.scenario.model;

public enum StorageTransferDirection {
    DEPOSIT("deposit", "Deposit Items"),
    WITHDRAW("withdraw", "Restock Items");

    private final String id;
    private final String label;

    StorageTransferDirection(
        String id,
        String label
    ) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }
}