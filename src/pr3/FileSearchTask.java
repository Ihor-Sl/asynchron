package pr3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSearchTask extends RecursiveTask<Integer> {
    private final File directory;
    private final long minSize;
    private final AtomicInteger foundFiles;

    public FileSearchTask(File directory, long minSize, AtomicInteger foundFiles) {
        this.directory = directory;
        this.minSize = minSize;
        this.foundFiles = foundFiles;
    }

    @Override
    protected Integer compute() {
        int localCount = 0;

        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }

        // Список підзадач для піддиректорій
        List<FileSearchTask> subTasks = new ArrayList<>();

        for (File file : files) {
            if (file.isFile()) {
                // Це файл - перевіряємо розмір
                long size = file.length();
                if (size > minSize) {
                    localCount++;
                    foundFiles.incrementAndGet();
                    System.out.printf("Знайдено: %s (%.2f MB)%n",
                            file.getAbsolutePath(),
                            size / (1024.0 * 1024.0));
                }
            } else if (file.isDirectory()) {
                // Це директорія - створюємо підзадачу
                FileSearchTask subTask = new FileSearchTask(file, minSize, foundFiles);
                subTasks.add(subTask);
                subTask.fork(); // Запускаємо асинхронно (Work Stealing)
            }
        }

        // Чекаємо завершення всіх підзадач
        for (FileSearchTask subTask : subTasks) {
            localCount += subTask.join();
        }

        return localCount;
    }
}
