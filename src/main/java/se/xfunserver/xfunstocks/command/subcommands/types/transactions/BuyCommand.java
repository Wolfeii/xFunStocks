package se.xfunserver.xfunstocks.command.subcommands.types.transactions;

import se.xfunserver.xfunstocks.command.subcommands.common.TransactionCommand;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.processor.StockProcessor;

public class BuyCommand extends TransactionCommand {

    public BuyCommand(StockProcessor stockProcessor, Messages messages) {
        super(stockProcessor, messages);
    }

    @Override
    public String commandName() {
        return "buy";
    }
}