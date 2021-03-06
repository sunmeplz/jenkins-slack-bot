package jenkins.plugins.bot;


import hudson.Extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

/**
 * {@link JBotCommand} to create a command alias.
 * 
 * @author kutzi
 */
@Extension
public class SetAliasCommand extends AbstractTextSendingCommand {
    @Override
    public Collection<String> getCommandNames() {
        return Collections.singleton("alias");
    }

    @Override
	protected String getReply(Bot bot, JBotSender sender, String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException();
		} else if (args.length == 1) {
			Map<String, AliasCommand> aliases = bot.getAliases();
			if (aliases.isEmpty()) {
				return "Defined aliases: none";
			} else {
				StringBuilder msg = new StringBuilder("Defined aliases:");
				for (Map.Entry<String, AliasCommand> entry : aliases.entrySet()) {
					msg.append("\n\t")
						.append(entry.getKey())
						.append(entry.getValue().getHelp());
				}
				return msg.toString();
			}
		} else if (args.length < 3) {
			String alias = args[1];
			AliasCommand aliasCmd = bot.removeAlias(alias);
			if (aliasCmd != null) {
				return "deleted alias: " + alias + aliasCmd.getHelp();
			} else {
				return sender.getNickname() + ": don't know an alias called '" + alias + "'";
			}
		} else {
			String alias = args[1];
			String cmdName = args[2];
			JBotCommand cmd = bot.getCommand(cmdName);
			if (cmd == null) {
				return sender.getNickname() + ": sorry don't know a command or alias called '" + cmdName + "'";
			}
			String[] cmdArguments = ArrayUtils.EMPTY_STRING_ARRAY;
			if (args.length > 3) {
				cmdArguments = MessageHelper.copyOfRange(args, 3, args.length);
			}
			
			AliasCommand aliasCmd = new AliasCommand(cmd, cmdName, cmdArguments);
			try {
				bot.addAlias(alias, aliasCmd);
			} catch (IllegalArgumentException e) {
				return sender.getNickname() + ": " + e.getMessage();
			}
			return "created alias: " + alias + aliasCmd.getHelp();
		}
	}

	public String getHelp() {
		return " [<alias> [<command>]] - defines a new alias, deletes one or lists all existing aliases";
	}

	/**
	 * An alias.
	 */
	public static class AliasCommand extends JBotCommand {

		private final JBotCommand command;
		private final String commandName;
		private final String[] arguments;

        public AliasCommand(JBotCommand cmd, String commandName, String[] arguments) {
			this.command = cmd;
			this.commandName = commandName;
			this.arguments = arguments;
		}
		
        @Override
        public Collection<String> getCommandNames() {
            return Collections.singleton(commandName);
        }

		public void executeCommand(Bot bot, JBotChat chat, JBotMessage message,
                                   JBotSender sender, String[] args) throws JBotException {
			String[] dynamicArgs = MessageHelper.copyOfRange(args, 1, args.length);
			
			String[] allArgs = MessageHelper.concat(new String[] {this.commandName}, this.arguments, dynamicArgs);
			System.out.println("Args: " + Arrays.toString(allArgs));
			
			this.command.executeCommand(bot, chat, message, sender, allArgs);
		}

		public String getHelp() {
			String help = " - alias for: '" + this.commandName;
			if (this.arguments.length > 0) {
				help += " " + MessageHelper.join(this.arguments, 0);
			}
			help += "'";
			return help;
		}
	}
}
