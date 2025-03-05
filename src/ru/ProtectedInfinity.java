package ru;

public class ProtectedInfinity {

    public static void process() {
        
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        String className = stackTrace[2].getClassName();

        try {
            
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(InfinityGuard.class)) {
                
                String nickname = UserData.getLogin();
                if (!InfinityGuardChecker.hasAccess(nickname)) {
                    throw new SecurityException("");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}