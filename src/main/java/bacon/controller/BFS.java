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
public class BFS extends Task{
    private Actor source;
    private Actor target;
    private int numberOfProcesses = 8;
    private MainController mainController;
    private int cycleLength = 500000;
    private int [] states;
    private Timeline checkProgress;
    private int cancelDepth = 0;
    private void signalState(int id, boolean state) {
        if(state) states[id]++;
        else states[id]=0;
    }
    private boolean finished = false;
    public  boolean isFinished(){
        return finished;
    }
    private String finalResult;
    private  BFSTask[] tasks;
    private Thread[] threads;
    BFS(MainController mainController) {
        this.mainController = mainController;
        states = new int[numberOfProcesses];
    }
    void init(){
        List<BlockingQueue<Element>> blockingQueue;
        blockingQueue = new ArrayList<>();
        tasks = new BFSTask[numberOfProcesses];
        threads = new Thread[numberOfProcesses];
        for (int i=0; i<numberOfProcesses; i++){
            blockingQueue.add(new PriorityBlockingQueue<>(100));
            tasks[i]=new BFSTask(i,blockingQueue);
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }
        Element temp = new Element(0);
        temp.add(source, source.getName());
        blockingQueue.get(0).add(temp);
    }

    @Override
    protected Object call(){
        checkProgress = new Timeline(new KeyFrame(Duration.millis(500), event -> {
//            System.out.println("checking if should cancel");
            boolean shouldCancel = false;
            for (int state : states) {

                if (state>100) {
                    System.out.println("I'm cancelling; state:" + state);
                    shouldCancel = true;
                    cancel();
                    break;
                }
            }
            if (shouldCancel){
                checkProgress.stop();
                for (BFSTask task: tasks){
                    task.cancel();
                }
            }
            updateMessage(getResult());
            int sum = 0;
            for (BFSTask task: tasks){
                ArrayList<Integer> partialProcessed = task.getProcessed();
                for (Integer integer : partialProcessed){
                    sum+=integer;
                }
            }
            System.out.println("total processed:" + sum);
        }));
        checkProgress.setCycleCount(Timeline.INDEFINITE);
        checkProgress.play();
        System.out.println("jesus fuck");
        System.out.println("joining stuff");
        for (Thread thread: threads){
            while(thread.isAlive()) {
                try {
                    thread.join(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("joined stuff prob");
        for (BFSTask task: tasks){
            System.out.println(task.id + " is cancelled " + task.isCancelled() + "\t is done " + task.isDone());
            finalResult+=task.getResult();
        }
        finished =  true;
        return null;
    }
    public String getCheckedMessage(){
        System.out.println("getting message");
        return super.getMessage();
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
//            System.out.println("not running");
            return finalResult;
        }
        else{
//            System.out.println("running");
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
        private int id;
        private List<BlockingQueue<Element>> blockingQueue;
        private List<ArrayList<Element>> output;
        private Queue<Element> internalQueue;
        private boolean active = true;
        private Set<Integer> visited = new HashSet<>();
        private String result = "";
        private ArrayList<Integer> processed;
        BFSTask(int id, List<BlockingQueue<Element>> blockingQueue){
            int initialMaxDepth = 20;
            this.id = id;
            this.blockingQueue = blockingQueue;
            internalQueue = new PriorityQueue<>();
            output = new ArrayList<>();
            processed = new ArrayList<>();
            for (int i=0; i<numberOfProcesses; i++){
                output.add(new ArrayList<>(initialMaxDepth){
                    @Override
                    public void add(int index, Element element) {
                        try {
                            super.add(index, element);
                        }catch (IndexOutOfBoundsException e){
                            ensureCapacity(index);
                            add(index, element);
                        }
                    }
                });
            }
        }

        @Override
        protected Object call(){
            while (!isCancelled()){
                boolean currentState = populateInternalQueue();
                for (Element element: internalQueue){
                    while (processed.size()<=element.getDepth()+1){
                        processed.add(0);
                    }
                    processed.set(element.getDepth(), processed.get(element.getDepth())+1);
                    if (element.peek().equals(target)){
                        System.out.println(id + "found target at depth " + element.getDepth());
                        if (cancelDepth == 0){
                            cancelDepth = element.getDepth();
                            result = element.peek().getPath()+"\n";
                        }
                    }
                    currentState = currentState || populateBlockingQueue(element);
                }
                if (currentState!= active)
                {
                    active = currentState;
                    signalState(id, active);
                    if(!active){
                        try {
                            Thread.sleep(cycleLength);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.println("returning " + id);
            return null;
        }
        String getResult(){
            System.out.println("trying to get result");
            return result;
        }
        ArrayList<Integer> getProcessed(){
            return processed;
        }
        @SuppressWarnings("Duplicates")
        private boolean populateBlockingQueue(Element element){

            int currentDepth = element.getDepth()+1;
            if (currentDepth > cancelDepth && cancelDepth != 0){    //remove excess elements
                internalQueue.remove(element);
                return false;
            }
            while (!element.isEmpty()){
                ActorContainer actorContainer = element.pop();
                String url = mainController.getRequestURL(MainController.RequestType.MoviesByActor, actorContainer.getID());
                ParallelRequest parallelRequest = new ParallelRequest(url);
                Thread thread = new Thread(parallelRequest);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Movie[] movies = mainController.parseMovies(parallelRequest.getResponse());
                for (Movie movie: movies){
                    url = mainController.getRequestURL(MainController.RequestType.ActorsByMovie, movie.getID());
                    parallelRequest = new ParallelRequest(url);
                    thread = new Thread(parallelRequest);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MovieFull movieFull = mainController.parseMovieFull(parallelRequest.getResponse());
                    for (Actor nextActor: movieFull.getActors()){
                        int actorId = Integer.parseInt(nextActor.getID().substring(2));
                        if (visited.contains(actorId)){
                            continue;
                        }
                        visited.add(actorId);
                        int remainder = actorId % numberOfProcesses;
                        String nextPath = actorContainer.getPath() + " starred in " + movieFull.getTitle()
                                + " with " + nextActor.getName() + '\n';
                        ActorContainer addedActor = new ActorContainer(nextActor, nextPath);
                        if (remainder!=id){     //provide item for other consumers
                            if  (output.get(remainder).get(currentDepth)==null){
                                output.get(remainder).add(new Element(currentDepth));
                            }
                            output.get(remainder).get(currentDepth).add(addedActor);
                        }
                        else{                   //provide for self
                            element.add(addedActor);
                        }
                        processed.set(currentDepth, processed.get(currentDepth)+1);
                    }
                }
            }
            return true;
        }
        private boolean populateInternalQueue(){
            boolean added = false;
            if (internalQueue.isEmpty()){
                Element element = new Element(00);
                try {
                    element = blockingQueue.get(id).poll(cycleLength*10, TimeUnit.MILLISECONDS);
                    if (element == null){
                        return false;
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                internalQueue.add(element);
                added = true;
                System.out.println(true);
            }
            return  added;
        }
    }
    class ActorContainer extends Actor {
        private String path;
        ActorContainer(Actor actor, String path) {
            super(actor);
            this.path = path;
        }

        String getPath() {
            return path;
        }
    }
    private class Element extends LinkedBlockingQueue<ActorContainer> implements  Comparable<Integer>{
        private int depth;

        Element(int depth) {
            super();
            this.depth = depth;
        }
        void add(Actor actor, String path){
            super.add(new ActorContainer(actor, path));
        }
        ActorContainer pop(){
            try {
                return super.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        int getDepth(){
            return depth;
        }
        @Override
        public int hashCode() {
            return depth;
        }

        @Override
        public int compareTo(Integer o) {
            return Integer.compare(depth, 0);
        }

    }
}
