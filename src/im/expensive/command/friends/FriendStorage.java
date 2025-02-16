package im.expensive.command.friends;

import im.expensive.utils.client.IMinecraft;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static java.io.File.separator;

@UtilityClass
public class FriendStorage implements IMinecraft {

    @Getter
    private final int color = 0x80FF80; // Цвет в формате RGB

    @Getter
    private final Set<String> friends = new HashSet<>();

    private final File file = new File(Minecraft.getInstance().gameDir, "astral" + separator + "files" + separator + "friends.cfg");

    static {
        load(); // Загрузка данных при инициализации класса
    }

    @SneakyThrows
    public static void load() {
        if (file.exists()) {
            friends.addAll(Files.readAllLines(file.toPath())); // Чтение строк из файла и добавление в коллекцию friends
        } else {
            file.createNewFile(); // Создание файла, если он не существует
        }
    }

    @SneakyThrows
    public static void save() {
        Files.write(file.toPath(), friends); // Записываем все строки из коллекции friends в файл
    }

    public void add(String name) {
        friends.add(name);
        save();
    }

    public void remove(String name) {
        friends.remove(name);
        save();
    }

    public void clear() {
        friends.clear();
        save();
    }

    public boolean isFriend(String name) {
        return friends.contains(name);
    }
}
