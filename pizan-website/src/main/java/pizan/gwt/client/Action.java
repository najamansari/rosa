package pizan.gwt.client;

import java.util.ArrayList;
import java.util.List;

// TODO refactor this and analytics, don't use enum so extensible

/**
 * Mapping between user actions and history tokens.
 */
public enum Action {
    HOME("home"), SEARCH("search"), BROWSE_BOOK("browse"), SELECT_BOOK("select"), READ_BOOK(
            "read"), VIEW_BOOK("book"), VIEW_PARTNERS("partners"), VIEW_PIZAN(
            "pizan"), VIEW_CONTACT("contact"), VIEW_WORKS(
            "works"), VIEW_TERMS("terms"), VIEW_COLLECTION_DATA("data"), VIEW_CHARACTER_NAMES(
            "chars"), VIEW_ILLUSTRATION_TITLES("illustrations"), VIEW_PROPER_NAMES("names");

    private final String prefix;

    Action(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Build up a token with the given arguments. Null arguments are ignored.
     * 
     * @param args
     * @return
     */

    public String toToken(String... args) {
        if (args.length == 0) {
            return prefix;
        }

        StringBuilder sb = new StringBuilder(prefix);

        // Escape ; with ;;

        for (String arg : args) {
            if (arg == null) {
                continue;
            }

            sb.append(';');

            if (arg.contains(";")) {
                sb.append(arg.replaceAll(";", ";;"));
            } else {
                sb.append(arg);
            }
        }

        return sb.toString();
    }

    public static List<String> tokenArguments(String token) {
        List<String> args = new ArrayList<String>(4);

        StringBuilder arg = new StringBuilder();
        boolean foundsemicolon = false;
        int i = token.indexOf(';');

        if (i == -1) {
            return args;
        }

        for (i++; i < token.length(); i++) {
            char c = token.charAt(i);

            if (foundsemicolon) {
                if (c != ';') {
                    args.add(arg.toString());
                    arg.setLength(0);
                }

                arg.append(c);
                foundsemicolon = false;
            } else {
                if (c == ';') {
                    foundsemicolon = true;
                } else {
                    arg.append(c);
                }
            }
        }

        if (arg.length() > 0) {
            args.add(arg.toString());
        }

        return args;
    }

    /**
     * @param token
     * @return corresponding value or null if the token is invalid
     */
    public static Action fromToken(String token) {
        int i = token.indexOf(';');

        if (i != -1) {
            token = token.substring(0, i);
        }

        for (Action state : Action.values()) {
            if (state.prefix.equals(token)) {
                return state;
            }
        }

        return null;
    }
}