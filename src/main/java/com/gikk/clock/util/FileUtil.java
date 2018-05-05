package com.gikk.clock.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;

public class FileUtil {
    private static final Path STORE_DIR = Paths.get("").resolve("files/");
    private static final Map<ObservableStringValue, ChangeListener> SYNC_MAP = new ConcurrentHashMap<>();

    static {
        File storeDir = STORE_DIR.toFile();
        if( !storeDir.exists() ) { storeDir.mkdir(); }
    }

    public static void sync(String fileName, ObservableStringValue value){
        sync(fileName, "", "", value);
    }

    public static void sync(String fileName, String prefix, String suffix, ObservableStringValue value){
        synchronized (SYNC_MAP){
            final Path filePath = STORE_DIR.resolve(fileName);
            flush(filePath, prefix+value.get()+suffix);

            //Remove the previous listener if there were one
            Optional.ofNullable( SYNC_MAP.get(value) ).ifPresent( l -> value.removeListener(l) );

            //Attach listener
            ChangeListener<String> l = (obs, ov, nv) -> flush(filePath, prefix+nv+suffix);
            value.addListener( l );

            SYNC_MAP.put(value, l);
        }
    }

    private static void flush(Path path, String data){
        try {
            Files.write(path,
                        data.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING );
        } catch (IOException ex) {
            System.err.println("Could not write file: " + path.toString());
        }
    }
}
