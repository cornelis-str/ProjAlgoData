import se.kth.id1020.Driver;
import se.kth.id1020.TinySearchEngineBase;

public class main {
    public static void main(String[] args) throws Exception{
        TinySearchEngineBase duckduckgoogle = new TinySearchEngine();
        Driver.run(duckduckgoogle);
    }
}