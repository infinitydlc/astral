
package ru;

import java.util.Arrays;
import net.minecraft.client.main.Main;
/*
Все что ниже делаешь все в главных классах

    перед классом пишешь @InfinityGuard
    ИМЕННО ПЕРЕД КЛАССОМ КАК НИЖЕ

Если у тебя expensive 2.0

    wtf.expensive.Initialization (или твой главный класс и ищешь там метод init)
    так же в Start классе (который в src метод main)
    в net.minecraft.client.main.Main (метод main)
    net.minecraft.client.Minecraft (метод Minecraft только ставишь в методе не в самом верху, а под super("Client");

Если у тебя expensive 3.1

    im.expensive.Expensive (или твой главный класс и ищешь там метод clientLoad)
    так же в Start классе (который в src)
    в net.minecraft.client.main.Main
    net.minecraft.client.Minecraft (метод Minecraft только ставишь в методе не в самом верху, а под super("Client");


ProtectedInfinity.process(); ставишь в верху метода (указаны выше в каких)
 */
@InfinityGuard
public class Exaple
{
    public static void main(String[] args)
    {
      /* ставишь в самом начале метода в классах указанные выше */  ProtectedInfinity.process();
        String assets = System.getenv().containsKey("assetDirectory") ? System.getenv("assetDirectory") : "assets";
        Main.main(concat(new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", assets, "--assetIndex", "1.16", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}

/*

ru.UserData.getLogin(); // получения логина
ru.UserData.getUid(); // получения логина
ru.UserData.getSub(); // получения даты истечения в софте


 */

