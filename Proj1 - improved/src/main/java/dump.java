

public class dump {
}

/*
import org.w3c.dom.Attr;
        import se.kth.id1020.TinySearchEngineBase;
        import se.kth.id1020.util.Word;
        import se.kth.id1020.util.Attributes;
        import se.kth.id1020.util.Document;

        import javax.print.Doc;
        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.LinkedList;
        import java.util.List;

public class TinySearchEngine implements TinySearchEngineBase {

    ArrayList<DocumentNode> index = new ArrayList<DocumentNode>(5);
    boolean unique;

    public void insert(Word word, Attributes attr){
        Node tmp = new Node(word, attr);
        //System.out.println("word: " + word);
        if(index.size() == 0){
            index.add(new DocumentNode(tmp.attr.document));
            index.get(0).firstChar.add(new FirstChar(tmp.word.word.charAt(0)));
            index.get(0).firstChar.get(0).nodeArray.add(tmp);
        } else {
            int m = docSearch(tmp.attr.document.name, index);
            if(unique){                                                                //behöver sätta sakerna på rätt plats överallt där m/n/o == -1, m == -1 => doc fanns inte
                index.add(m, new DocumentNode(tmp.attr.document));
                index.get(m).firstChar.add(new FirstChar(tmp.word.word.charAt(0)));                             //.get(positionen new doc ska ligga på)
                index.get(m).firstChar.get(0).nodeArray.add(tmp);                                               //.get(0) är rätt
            } else {
                int n = fcharSearch(tmp.word.word.charAt(0), index.get(m).firstChar);              //n == -1 => fchar fanns inte tidigare
                if(unique){
                    index.get(m).firstChar.add(n, new FirstChar(tmp.word.word.charAt(0)));
                    index.get(m).firstChar.get(n).nodeArray.add(tmp);                                           //på vilken bokstav börjar den
                } else {
                    int o = NodeSearch(tmp.word.word, index.get(m).firstChar.get(n).nodeArray);     //o == -1 => ordet fanns inte sen tidigare
                    index.get(m).firstChar.get(n).nodeArray.add(o, tmp);                                      //på vilken plats ska denna läggas till på

                }
            }
        }
    }

    public List<Document> search (String query){
        //test:
        //printIndex();

        boolean sort = false;
        List<Document> ret;
        if(query.contains("orderby")){                                            //filter out "/command" and save it for sorting later
            sort = true;
        }
        if(query.contains(" ")){                                            //union search!
            ret = unionSearch(query);
        } else {
            ret = simplesearch(query);
        }
        if(sort){
            //call sort method with condition
        }
        return ret;
    }

    ArrayList<LinkedList<Document>> union = new ArrayList<LinkedList<Document>>();

    public List<Document> unionSearch (String query){
        union.clear();

        String[] querySplit = query.split(" ");
        for(int i = 0; i < querySplit.length; i++) {

            List<Document> tmp = simplesearch(querySplit[i]);
            LinkedList<Document> tmp2 = new LinkedList<Document>();
            for(int j = 0; j < tmp.size(); j++){
                tmp2.add(tmp.get(j));
            }
            union.add(tmp2);
        }

        while(union.size() > 1){
            LinkedList<Document> tmp = merge(union.get(0), union.get(1));
            union.remove(0);
            union.remove(0);
            union.add(tmp);
        }
        return union.get(0);
    }

    public LinkedList<Document> merge (List<Document> a, List<Document> b){
        LinkedList<Document> ret = new LinkedList<Document>();
        ret.addAll(a);

        for(int i = 0; i < b.size(); i++){
            if(!ret.contains(b.get(i))){
                ret.add(b.get(i));
            }
        }
        return ret;
    }

    LinkedList<Document> simple = new LinkedList<Document>();

    public LinkedList<Document> simplesearch (String query){
        simple.clear();
        for(DocumentNode docNode : index){
            simplesearchhelper(query, docNode);
        }
        return simple;
    }

    public void simplesearchhelper(String query, DocumentNode docNode){
        int m = fcharSearch(query.charAt(0), docNode.firstChar);
        if(!unique) {
            int n = NodeSearch(query, docNode.firstChar.get(m).nodeArray);
            if(!unique){
                simple.add(docNode.firstChar.get(m).nodeArray.get(n).attr.document);
            }
        }
    }

    public void size(){
        int size = 0;
        for(int i = 0; i < index.size(); i++) {
            for (int j = 0; j < index.get(i).firstChar.size(); j++) {
                for (int k = 0; k < index.get(i).firstChar.get(j).nodeArray.size(); k++) {
                    size++;
                }
            }
        }
        System.out.println("Number of words in the index: " + size);
    }

    public void printIndex(){
        for(int j = 0; j < index.get(0).firstChar.size(); j++){
            for(int k = 0; k < index.get(0).firstChar.get(j).nodeArray.size(); k++){
                System.out.println(index.get(0).firstChar.get(j).nodeArray.get(k).word.word + "\t\t\t" +  index.get(0).firstChar.get(j).nodeArray.get(k).attr.occurrence + "\t\t\t" + index.get(0).firstChar.get(j).nodeArray.get(k).attr.document.name);
            }
        }
        /*
        for(int i = 0; i < index.size(); i++){
            for(int j = 0; j < index.get(i).firstChar.size(); j++){
                for(int k = 0; k < index.get(i).firstChar.get(j).nodeArray.size(); k++){
                    System.out.println(index.get(i).firstChar.get(j).nodeArray.get(k).word.word + "\t\t\t" +  index.get(i).firstChar.get(j).nodeArray.get(k).attr.occurrence + "\t\t\t" + index.get(i).firstChar.get(j).nodeArray.get(k).attr.document.name);
                }
            }
        }
        */

    /*

    public int docSearch(String key, ArrayList<DocumentNode> documentNodes) {
        return docSearch(key.toLowerCase(), documentNodes, 0, documentNodes.size());
    }
    public int docSearch(String key, ArrayList<DocumentNode> documentNodes, int lo, int hi) {
        if (hi <= lo) {
            unique = true;
            return lo;                                                                         //return hi and false if the word doesn't exist.
        }
        int mid = lo + (hi - lo) / 2;
        int cmp = documentNodes.get(mid).doc.name.toLowerCase().compareTo(key);
        if      (cmp > 0) return docSearch(key, documentNodes, lo, mid);
        else if (cmp < 0) return docSearch(key, documentNodes, mid+1, hi);
        else {
            unique = false;
            return mid;
        }
    }

    //to search for the correct position in the documents char array
    public int fcharSearch(char key, ArrayList<FirstChar> firstChars) {
        return fcharSearch(Character.toLowerCase(key), firstChars, 0, firstChars.size());
    }
    public int fcharSearch(char key, ArrayList<FirstChar> firstChars, int lo, int hi) {
        if (hi <= lo) {
            unique = true;
            return lo;                                                                         //return hi and true if the word doesn't exist.
        }
        int mid = lo + (hi - lo) / 2;
        int cmp = Character.toLowerCase(firstChars.get(mid).c) - key;
        if      (cmp > 0) return fcharSearch(key, firstChars, lo, mid);
        else if (cmp < 0) return fcharSearch(key, firstChars, mid+1, hi);
        else {
            unique = false;
            return mid;
        }
    }

    //to search for the correct position in the NodeArray
    public int NodeSearch(String key, ArrayList<Node> nodeArray) {
        return NodeSearch(key.toLowerCase(), nodeArray, 0, nodeArray.size());
    }
    public int NodeSearch(String key, ArrayList<Node> nodeArray, int lo, int hi) {
        if (hi <= lo) {
            unique = true;
            return lo;                                                                         //return hi and true if the word doesn't exist.
        }
        int mid = lo + (hi - lo) / 2;
        int cmp = nodeArray.get(mid).compareTo(key);
        if      (cmp > 0) return NodeSearch(key, nodeArray, lo, mid);
        else if (cmp < 0) return NodeSearch(key, nodeArray, mid+1, hi);
        else {
            unique = false;
            return mid;
        }
    }

    public class DocumentHolder implements Comparable{
        public Word word;
        public Attributes attr;
        int count;

        public DocumentHolder(Word word, Attributes attr, int count){
            this.word = word;
            this.attr = attr;
            this.count = count;
        }

        public int compareTo(Object o) {
            return 0;
        }

        public int compareTo(int count){
            return this.count - count;
        }

        public int compareTo(Attributes attr){
            return this.attr.occurrence - attr.occurrence;
        }
    }

    public class DocumentNode implements Comparable{
        Document doc;
        ArrayList<FirstChar> firstChar = new ArrayList<FirstChar>();

        public DocumentNode(Document doc){
            this.doc = doc;
        }

        public int compareTo(DocumentNode docN){
            return doc.name.compareTo(docN.doc.name);
        }

        public int compareTo(Object o) {
            return 0;
        }
    }

    public class FirstChar implements Comparable{
        char c;
        ArrayList<Node> nodeArray = new ArrayList<Node>();

        public FirstChar(char c){
            this.c = c;
        }

        public int compareTo(Object o) {
            return 0;
        }

        public int compareTo(FirstChar firstChar){
            return c - firstChar.c;
        }
    }

    public class Node implements Comparable{
        Word word;
        Attributes attr;

        public Node(Word word, Attributes attr){
            this.word = word;
            this.attr = attr;
        }

        public int compareTo(Object cmp) {
            return 0;
        }

        public int compareTo(Node n){
            return word.word.compareTo(n.word.word);
        }

        public int compareTo(String cmp){
            return word.word.toLowerCase().compareTo(cmp);
        }
    }
}

*/

    /*
    //to search for the right document:
    public int BinarySearchDA(String key, ArrayList<TinySearchEngine.DocumentNode> documentNodes) {
        return BinarySearchDA(key, documentNodes, 0, documentNodes.size());
    }
    public int BinarySearchDA(String key, ArrayList<TinySearchEngine.DocumentNode> documentNodes, int lo, int hi) {
        System.out.println("lo  = " + lo);
        System.out.println("hi  = " + hi);
        // possible key indices in [lo, hi)
        if (hi <= lo) return -1;                            //return lo if the word doesn't exist.
        int mid = lo + (hi - lo) / 2;
        int cmp = documentNodes.get(mid).doc.name.compareTo(key);
        if      (cmp > 0) return BinarySearchDA(key, documentNodes, lo, mid);
        else if (cmp < 0) return BinarySearchDA(key, documentNodes, mid+1, hi);
        else              return mid;
    }

    //to search for the correct position in the documents char array
    public int BinarySearchFS(char key, ArrayList<TinySearchEngine.FirstChar> firstChars) {
        return BinarySearchFS(key, firstChars, 0, 0);
    }
    public int BinarySearchFS(char key, ArrayList<TinySearchEngine.FirstChar> firstChars, int lo, int hi) {
        System.out.println("lo  = " + lo);
        System.out.println("hi  = " + hi);
        // possible key indices in [lo, hi)
        if (hi <= lo) return -1;                            //return lo if the word doesn't exist.
        int mid = lo + (hi - lo) / 2;
        int cmp = firstChars.get(mid).c - key;
        if      (cmp > 0) return BinarySearchFS(key, firstChars, lo, mid);
        else if (cmp < 0) return BinarySearchFS(key, firstChars, mid+1, hi);
        else              return mid;
    }

    //to search for the correct position in the NodeArray
    public int BinarySearchNA(String key, ArrayList<TinySearchEngine.Node> nodeArray) {
        return BinarySearchNA(key, nodeArray, 0, nodeArray.size());
    }
    public int BinarySearchNA(String key, ArrayList<TinySearchEngine.Node> nodeArray, int lo, int hi) {
        System.out.println("lo  = " + lo);
        System.out.println("hi  = " + hi);
        // possible key indices in [lo, hi)
        if (hi <= lo) return -1;                            //return lo if the word doesn't exist.
        int mid = lo + (hi - lo) / 2;
        int cmp = nodeArray.get(mid).compareTo(key);
        if      (cmp > 0) return BinarySearchNA(key, nodeArray, lo, mid);
        else if (cmp < 0) return BinarySearchNA(key, nodeArray, mid+1, hi);
        else              return mid;
    }
}


public class something{

if(index.size() == 0){                                                                                 //no documents exist.
    index.add(new DocumentNode(tmp.attr.document));
    index.get(0).firstChar.add(new FirstChar(tmp.word.word.charAt(0)));
    index.get(0).firstChar.get(0).nodeArray.add(tmp);
} else {
    BinarySearch.BoolInt tmp2 = BinarySearch.BinarySearchDA(tmp.attr.document.name, index);
    //System.out.println(tmp2.bool + " " +  tmp2.val);
    if(!tmp2.bool) {                                                                                 //the doc doesn't exist
        index.add(tmp2.val, new DocumentNode(tmp.attr.document));
        index.get(tmp2.val).firstChar.add(new FirstChar(tmp.word.word.charAt(0)));
        index.get(tmp2.val).firstChar.get(0).nodeArray.add(tmp);
    } else {
        BinarySearch.BoolInt tmp3 = BinarySearch.BinarySearchFS(tmp.word.word.charAt(0), index.get(tmp2.val).firstChar);
        //System.out.println("Tmp3 = " + tmp3.bool + " " + tmp3.val);
        if(tmp3.val == -1){
            index.get(tmp2.val).firstChar.add(new FirstChar(tmp.word.word.charAt(0)));
            index.get(tmp2.val).firstChar.get(0).nodeArray.add(tmp);
        } else if (!tmp3.bool && tmp3.val != -1) {                                                                             //the doc exists but no words exists with the first letter
            //System.out.println("Size of firstchar: " + index.get(tmp2.val).firstChar.size());
            index.get(tmp2.val).firstChar.add(tmp3.val, new FirstChar(tmp.word.word.charAt(0)));
            index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray.add(tmp);
        } else {
            BinarySearch.BoolInt tmp4 = BinarySearch.BinarySearchNA(tmp.word.word, index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray);
            if(tmp4.val == -1){
                index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray.add(tmp);
            }else if (!tmp4.bool && tmp4.val != -1) {                                                                         //the doc exists and there are words with the same first letter but not the exact same word.
                index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray.add(tmp4.val, tmp);
            } else {                        //the word exists
                int occ = index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray.get(tmp4.val).attr.occurrence;
                index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray.remove(tmp4.val);
                index.get(tmp2.val).firstChar.get(tmp3.val).nodeArray.add(tmp4.val, new Node(word, new Attributes(attr.document, occ)));
            }
        }
        }



*/

/*


    import java.util.Collections;
            import java.util.Iterator;
            import java.util.LinkedList;
            import java.util.ListIterator;

public class Sort {

    public static LinkedList<TinySearchEngine.DocumentHolder> Bubblesort(LinkedList<TinySearchEngine.DocumentHolder> list, int n){
        if(list.isEmpty()){
            throw new NullPointerException("Is your head as empty as this list?");
        }

        if(n == 0) {
            ListIterator<TinySearchEngine.DocumentHolder> iter = list.listIterator();
            int R = list.size() - 2;
            boolean swapped = true;
            while (R >= 0 && swapped) {
                swapped = false;
                for (int i = 0; i <= R; i++) {
                    if (iter.next().attr.occurrence - iter.next().attr.occurrence < 0) {
                        swapped = true;
                        Collections.swap(list, i, i + 1);
                    }
                    iter.previous();
                }
                iter = list.listIterator();
                R--;
            }
            return list;
        } else if(n == 1) {
            ListIterator<TinySearchEngine.DocumentHolder> iter = list.listIterator();
            int R = list.size() - 2;
            boolean swapped = true;
            while (R >= 0 && swapped) {
                swapped = false;
                for (int i = 0; i <= R; i++) {
                    if (iter.next().count - iter.next().count < 0) {
                        swapped = true;
                        Collections.swap(list, i, i + 1);
                    }
                    iter.previous();
                }
                iter = list.listIterator();
                R--;
            }
            return list;
        }
        if(n == 2) {
            ListIterator<TinySearchEngine.DocumentHolder> iter = list.listIterator();
            int R = list.size() - 2;
            boolean swapped = true;
            while (R >= 0 && swapped) {
                swapped = false;
                for (int i = 0; i <= R; i++) {
                    if (iter.next().attr.document.popularity - iter.next().attr.document.popularity < 0) {
                        swapped = true;
                        Collections.swap(list, i, i + 1);
                    }
                    iter.previous();
                }
                iter = list.listIterator();
                R--;
            }
            return list;
        }

    }
}


*/

/*
    public void size(){
        int size = 0;
        for(int i = 0; i < index.size(); i++) {
            for (int j = 0; j < index.get(i).firstChar.length; j++) {
                for (int k = 0; k < index.get(i).firstChar[j].nodeArray.size(); k++) {
                    size++;
                }
            }
        }
        System.out.println("Number of words in the index: " + size);
    }

    public void printIndex(){
        for(int j = 0; j < index.get(0).firstChar.length; j++){
            for(int k = 0; k < index.get(0).firstChar[j].nodeArray.size(); k++){
                System.out.println(index.get(0).firstChar[j].nodeArray.get(k).word.word + "\t\t\t" +  index.get(0).firstChar.get(j).nodeArray.get(k).attr.occurrence + "\t\t\t" + index.get(0).firstChar.get(j).nodeArray.get(k).attr.document.name);
            }
        }

        for(int i = 0; i < index.size(); i++){
            for(int j = 0; j < index.get(i).firstChar.size(); j++){
                for(int k = 0; k < index.get(i).firstChar.get(j).nodeArray.size(); k++){
                    System.out.println(index.get(i).firstChar.get(j).nodeArray.get(k).word.word + "\t\t\t" +  index.get(i).firstChar.get(j).nodeArray.get(k).attr.occurrence + "\t\t\t" + index.get(i).firstChar.get(j).nodeArray.get(k).attr.document.name);
                }
            }
        }

    }

*/