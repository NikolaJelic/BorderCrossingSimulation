package com.nikola.bordercrossingsimulator.models.terminal;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.models.Simulation;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;

public class TerminalStatusWatcher extends Thread{
    final ArrayList<Terminal> terminals;
    Path path;
    Path dir;

    public TerminalStatusWatcher(Path path, ArrayList<Terminal> terminals) {
        this.path = path;
        this.dir = path.getParent();
        this.terminals = terminals;
    }

    @Override
    public void run(){
        try{
            WatchService watchService = FileSystems.getDefault().newWatchService();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while(!Simulation.isFinished()){
                WatchKey watchKey = null;

                try{

                    watchKey = watchService.poll(1000, TimeUnit.MILLISECONDS);
                }
                catch(Exception e){
                    Main.logger.log(Level.WARNING, e.getMessage());
                }

                if(watchKey != null){
                    for(WatchEvent<?> event:watchKey.pollEvents()){
                        WatchEvent.Kind<?> kind = event.kind();
                        WatchEvent<Path> eventPath = (WatchEvent<Path>)event;
                        Path evPath = eventPath.context();

                        if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY) && evPath.equals(path.getFileName())){
                            System.out.println("Change detected at " + evPath);

                            readFile(path);
                        }
                    }

                    boolean status = watchKey.reset();
                    if(!status){
                        break;
                    }
                }
            }
        }
        catch(Exception e){
            Main.logger.log(Level.WARNING, e.getMessage());
        }
    }

    private void readFile(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                String[] keyValuePair = line.split("=", 2);
                if (keyValuePair.length == 2) {
                    synchronized (terminals) {
                        terminals.get(Integer.parseInt(keyValuePair[0])).setStatus(Integer.parseInt(keyValuePair[1]) != 0);
                    }
                }
            });
        } catch (Exception e) {
            Main.logger.log(Level.SEVERE, e.getMessage());
        }
    }


}
