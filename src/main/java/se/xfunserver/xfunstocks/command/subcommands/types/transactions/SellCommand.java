package se.xfunserver.xfunstocks.command.subcommands.types.transactions;

import se.xfunserver.xfunstocks.command.subcommands.common.TransactionCommand;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.processor.StockProcessor;

public class SellCommand extends TransactionCommand {

    public SellCommand(StockProcessor stockProcessor, Messages messages) {
        super(stockProcessor, messages);
    }

    @Override
    public String commandName() {
        return "sell";
    }
}
