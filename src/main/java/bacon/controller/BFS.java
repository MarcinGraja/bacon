package bacon.controller;

import bacon.model.Actor;
import bacon.model.Movie;
import bacon.model.MovieFull;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BFS extends Task{
    private Actor source;
    private Actor target;
    private int numberOfProcesses = 20;
    private MainController mainController;
    private Timeline checkProgress;
    private final AtomicInteger cancelDepth = new AtomicInteger();
    private boolean finished = false;
    boolean isFinished(){
        return finished;
    }
    private String finalResult;
    private  BFSTask[] tasks;
    private Thread[] threads;
    private BlockingQueue<Element> blockingQueue;
    private SetBlockingQueue<Integer> visited = new SetBlockingQueue<>();
    BFS(MainController mainController) {
        this.mainController = mainController;
    }
    void init(){

        blockingQueue = new PriorityBlockingQueue<>();
        tasks = new BFSTask[numberOfProcesses];
        threads = new Thread[numberOfProcesses];
        for (int i=0; i<numberOfProcesses; i++){
            tasks[i]=new BFSTask();
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }
        Element temp = new Element(0, source, source.getName());
        try {
            blockingQueue.put(temp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected Object call(){
        final AtomicInteger prevSum = new AtomicInteger(0);
        checkProgress = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            boolean wantsToCancel = true;
            if (!blockingQueue.isEmpty()){
                wantsToCancel = false;
            }
            else{
                for (Task task: tasks){
                    if(task.getMessage().equals("working")) wantsToCancel = false;
                }
            }
            if (wantsToCancel){
                checkProgress.stop();
                for (BFSTask task: tasks){
                    task.cancel();
                }
            }
            int sum = 0;
            for (BFSTask task: tasks){
                ArrayList<Integer> partialProcessed = task.getProcessed();
                for (Integer integer : partialProcessed){
                    sum+=integer;
                }
            }
            updateMessage(getResult()+"\n left: " + blockingQueue.size() + " first in queue at depth "
                    + (blockingQueue.peek()!=null? blockingQueue.peek().getDepth() : "unknown")
                    + "\nper second: " + 2.0*(sum- prevSum.get()));
            prevSum.set(sum);
        }));
        checkProgress.setCycleCount(Timeline.INDEFINITE);
        checkProgress.play();
        for (Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (BFSTask task: tasks){
            finalResult+=task.getResult();
            System.out.println("Partial result: "+task.getResult());
        }
        updateMessage(finalResult);
        finished =  true;
        return null;
    }
    @Override
    public boolean cancel(boolean b) {
        for (Task task: tasks){
            task.cancel();
        }
        return super.cancel(b);
    }

    private String getResult(){
        if (!isRunning()){
            System.out.println("final result given");
            return finalResult;
        }
        else{
            StringBuilder partialResult = new StringBuilder("processed: \n");
            ArrayList<Integer> processed = new ArrayList<>();
            for (BFSTask task: tasks){
                ArrayList<Integer> partialProcessed = task.getProcessed();
                for (int i=0; i<partialProcessed.size(); i++){
                    while (processed.size()<=i){
                        processed.add(0);
                    }
                    processed.set(i,processed.get(i)+partialProcessed.get(i));
                }
            }
            for(int i=0; i<processed.size(); i++){
                partialResult.append(processed.get(i)).append(" at depth ").append(i).append("\n");
            }
            return partialResult.toString();
        }
    }

    void setSource(Actor source) {
        this.source = source;
    }

    void setTarget(Actor target) {
        this.target = target;
    }
    private class BFSTask extends Task{
        private String result = "";
        private ArrayList<Integer> processed;
        BFSTask(){
            processed = new ArrayList<>();
        }
        @Override
        protected Object call() {
            try {
                while (!isCancelled()) {
                    Element element;
                    try {
                        updateMessage("waiting");
                        element = blockingQueue.take();
                        updateMessage("working");
                        if (visited.contains(Integer.parseInt(element.getID().substring(2)))
                            || element.getDepth()>cancelDepth.get() && cancelDepth.get()!=0){
                            System.out.println("continued1");
                            continue;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                    while (processed.size() <= element.getDepth() + 1) {
                        processed.add(0);
                    }
                    processed.set(element.getDepth(), processed.get(element.getDepth()) + 1);//count number of processed items
                    if (element.equals(target)) {
                        System.out.println("found target at depth " + element.getDepth() + " with path " + element.getPath()
                                            + "cancel depth: " + cancelDepth.get());
                        if (cancelDepth.get() == 0 || cancelDepth.get() > element.getDepth()) {
                            cancelDepth.set(element.getDepth());
                            result += element.getPath() + "\n";
                        }
                    }
                    if (element.getDepth()>=cancelDepth.get() && cancelDepth.get()!=0){
                        System.out.println("continued2");
                        continue;
                    }
                    visited.add(Integer.parseInt(element.getID().substring(2)));
                    populateBlockingQueue(element);
                }
//                System.out.println("cancelled apparently");
//                if (isCancelled()) System.out.println("definitely");
                return null;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        String getResult(){
            return result;
        }
        ArrayList<Integer> getProcessed(){
            return processed;
        }
        @SuppressWarnings("Duplicates")
        private void populateBlockingQueue(Element element){

            int currentDepth = element.getDepth()+1;
            if (currentDepth > cancelDepth.get() && cancelDepth.get() != 0){    //remove excess elements
                try {
                    blockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            String url = mainController.getRequestURL(MainController.RequestType.MoviesByActor, element.getID());
            ParallelRequest parallelRequest = new ParallelRequest(url);
            Thread thread = new Thread(parallelRequest);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String json = parallelRequest.getResponse();
            if (json == null){
                try {
                    blockingQueue.put(element);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            Movie[] movies = mainController.parseMovies(json);
            for(Movie movie: movies) {
                url = mainController.getRequestURL(MainController.RequestType.ActorsByMovie, movie.getID());
                parallelRequest = new ParallelRequest(url);
                thread = new Thread(parallelRequest);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                json = parallelRequest.getResponse();
                if (json == null){

                    try {
                        blockingQueue.put(element);
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                MovieFull movieFull = mainController.parseMovieFull(json);
                for (Actor actor: movieFull.getActors()){
                    int actorId = Integer.parseInt(actor.getID().substring(2));
                    String nextPath = element.getPath() + " starred in " + movieFull.getTitle()
                            + " with " + actor.getName() + '\n';
                    ActorContainer addedActor = new ActorContainer(actor, nextPath);
                    try {
                        blockingQueue.put(new Element(currentDepth, addedActor));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    processed.set(currentDepth, processed.get(currentDepth)+1);
//                    if (currentDepth == 3){
//                        System.out.println(addedActor.getID() + "\t" + addedActor.getName());
//                    }
                }

            }

        }
    }
    class ActorContainer extends Actor {
        private String path;
        ActorContainer(Actor actor, String path) {
            super(actor);
            this.path = path;
        }

        public ActorContainer(ActorContainer addedActor) {
            super(addedActor.getName(), addedActor.getID());
            this.path=addedActor.getPath();
        }

        String getPath() {
            return path;
        }
    }
    public class SetBlockingQueue<T> extends LinkedBlockingQueue<T> {

        private Set<T> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

        /**
         * Add only element, that is not already enqueued.
         * The method is synchronized, so that the duplicate elements can't get in during race condition.
         * @param t object to put in
         * @return true, if the queue was changed, false otherwise
         */
        @Override
        public synchronized boolean add(T t) {
            if (set.contains(t)) {
                return false;
            } else {
                set.add(t);
                return super.add(t);
            }
        }

        /**
         * Takes the element from the queue.
         * Note that no synchronization with {@link #add(Object)} is here, as we don't care about the element staying in the set longer needed.
         * @return taken element
         * @throws InterruptedException
         */
        @Override
        public T take() throws InterruptedException {
            T t = super.take();
            set.remove(t);
            return t;
        }
    }
    private class Element extends ActorContainer implements  Comparable<Element>{
        private int depth;

        Element(int depth, Actor actor, String path) {
            super(actor, path);
            this.depth = depth;
        }

        public Element(int currentDepth, ActorContainer addedActor) {
            super(addedActor);
            this.depth=currentDepth;
        }

        int getDepth(){
            return depth;
        }
        @Override
        public int hashCode() {
            return depth;
        }
        @Override
        public int compareTo(Element o) {
            return Integer.compare(depth, o.getDepth());
        }

    }
}
