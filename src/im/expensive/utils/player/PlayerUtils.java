package im.expensive.utils.player;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class PlayerUtils {

    private final Pattern NAME_REGEX = Pattern.compile("^[A-Za-zА-Яа-я0-9_]{3,16}$");
    public boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }
}
    