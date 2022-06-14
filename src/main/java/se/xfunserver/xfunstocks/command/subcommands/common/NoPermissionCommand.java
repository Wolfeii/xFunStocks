package se.xfunserver.xfunstocks.command.subcommands.common;

import se.xfunserver.xfunstocks.config.Messages;

public abstract class NoPermissionCommand extends BaseCommand {

    protected NoPermissionCommand(Messages messages) {
        super(messages);
    }

    @Override
    public String requiredPerm() {
        return null;
    }
}