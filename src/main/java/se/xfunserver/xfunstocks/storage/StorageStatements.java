package se.xfunserver.xfunstocks.storage;

public abstract class StorageStatements {

    private static final String SELECT_ALL = "SELECT id, uuid, type, date, symbol, quantity, "
            + "single_price, earnings, sold FROM xfunstock_transactions";

    protected abstract String getCreateTableQuery();

    protected abstract String getLastInsertQuery();

    protected String getPurchaseQuery() {
        return "INSERT INTO xfunstock_transactions (uuid, type, date, symbol, quantity, "
                + "single_price) VALUES (?, 'PURCHASE', ?, ?, ?, ?);";
    }

    protected String getSaleQuery() {
        return "INSERT INTO xfunstock_transactions (uuid, type, date, symbol, quantity, single_price, "
                + "earnings) VALUES (?, 'SALE', ?, ?, ?, ?, ?);";
    }

    protected String getPlayerTransactionsQuery() {
        return SELECT_ALL + " WHERE uuid = ? ORDER BY date";
    }

    protected String getStockTransactionsQuery() {
        return SELECT_ALL + " WHERE symbol = ? ORDER BY date";
    }

    protected String getRecentTransactionsQuery() {
        return SELECT_ALL + " ORDER BY date LIMIT 100";
    }

    protected String getMarkSoldQuery() {
        return "UPDATE xfunstock_transactions SET sold = true WHERE id = ?";
    }
}