import org.omg.CORBA.UNSUPPORTED_POLICY;
import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;
import java.util.*;

public class TinySearchEngine implements TinySearchEngineBase {

    HashMap<String, ArrayList<Node>> index = new HashMap<String, ArrayList<Node>>();
    HashMap<String, ArrayList<Node>> subqueries = new HashMap<String, ArrayList<Node>>();
    HashMap<String, Integer> docWordCount = new HashMap<String, Integer>();
    double numberOfDocs = 0.0;

    public void preInserts() {

    }

    public void insert(Sentence sentence, Attributes attributes) {
        Iterator<Word> it= sentence.getWords().listIterator();
        while(it.hasNext()){
            insert(it.next(), attributes);
        }
    }

    public void insert(Word word, Attributes attr){
        String key = word.word.toLowerCase();
        if(index.containsKey(key)){
            ArrayList<Node> tmplist = index.get(key);
            index.remove(key);
            Iterator<Node> it = tmplist.listIterator();
            boolean unique = true;
            while(it.hasNext() && unique){
                Node tmpNode = it.next();
                if(tmpNode.attr.document.name.equals(attr.document.name)){
                    tmpNode.count++;
                    unique = false;
                }
            }
            if(unique){
                tmplist.add(new Node(word, attr));
            }
            index.put(key, tmplist);
        } else {
            ArrayList<Node> list = new ArrayList<Node>();
            list.add(new Node(word, attr));
            index.put(key, list);
        }
        //used later for relevance.
        if(docWordCount.containsKey(attr.document.name)){
            int n = docWordCount.remove(attr.document.name);
            docWordCount.put(attr.document.name, n + 1);
        } else {
            docWordCount.put(attr.document.name, 1);
            numberOfDocs += 1;
        }
    }

    public void postInserts() {
        System.out.println("Size of values in index: " + index.values().size());
        for(ArrayList<Node> arrayList : index.values()){
            for(Node n : arrayList){
                double count = (double) n.count;
                double countindoc = (double) docWordCount.get(n.attr.document.name);
                n.relevance = count/countindoc * Math.log10(numberOfDocs/arrayList.size());
            }
        }
    }

    String infix = "";

    public String infix(String s) {
        return infix;
    }

    public List<Document> search(String s) {
        infix = "";
        s = s.toLowerCase();
        String[] query = s.split(" ");

        //sorting criteria:
        boolean sort = false;
        boolean sortbypop = false;
        boolean sortbyrel = false;
        boolean asc = true;
        String lastpart = "";
        for(int i = 0; i < query.length; i++) {
            if (query[i].equals("orderby")) {
                sort = true;
                if (i + 1 > query.length) {
                    throw new UnsupportedOperationException("???");
                } else {
                    if (query[i + 1].equals("relevance")) {
                        sortbyrel = true;
                    } else if (query[i + 1].equals("popularity")) {
                        sortbypop = true;
                    }
                    if (i + 2 < query.length) {
                        if (query[i + 2].equals("desc")) {
                            asc = false;
                        }
                    }
                    //remove words
                    //search with same conditions as before with the new query that has been split and "orderby" and "desc" / "asc" have been removed
                    String[] tmpquery = new String[i];
                    for (int j = 0; j < i; j++) {
                        tmpquery[j] = query[j];
                    }
                    for(int j = i; j < i+3; j++){
                        lastpart += " " + query[j];
                    }
                    query = tmpquery;
                }
            }
        }

        //For executing the actual search:
        Stack<ParseElement> stack = new Stack<ParseElement>();
        Stack<String> strings = new Stack<String>();

        for (String part : query) {
            strings.push(part);
            if (part.equals("+") || part.equals("-") || part.equals("|")) {
                stack.push(new ParseElement(part));
            } else {
                stack.push(new ParseElement(index.get(part), part));
            }

            boolean checked = false;

            while (stack.size() > 2 && !checked) {
                ParseElement p2 = stack.pop();
                ParseElement p1 = stack.pop();
                if (p1.operator == null && p2.operator == null) {
                    String op = stack.pop().operator;
                    if(op == null){
                        System.out.println("You seem to have entered a faulty query, please try again.");
                        return null;
                    }
                    ArrayList<Node> result = new ArrayList<Node>();
                    //kollar om subqueryn queryn redan finns, annars utföra operationen
                    if (subqueries.containsKey("(" + p1.name + " " + op + " " + p2.name + ")")) {
                        System.out.println("Subquery found: " + "(" + p1.name + " " + op + " " + p2.name + ")");
                        result = subqueries.get("(" + p1.name + " " + op + " " + p2.name + ")");
                    } else if ((op.equals("+") || op.equals("|")) && subqueries.containsKey(p2.name + " " + op + " " + p1.name)) {
                        System.out.println("Subquery found because of symmetry: " + "(" + p1.name + " " + op + " " + p2.name + ")" + " is equal to: " + "(" + p1.name + " " + op + " " + p2.name + ")");
                        result = subqueries.get("(" + p1.name + " " + op + " " + p2.name + ")");
                    } else {
                        if (op.equals("+")) {
                            result = intersection(p1.arrlist, p2.arrlist);
                        } else if (op.equals("|")) {
                            result = union(p1.arrlist, p2.arrlist);
                        } else if (op.equals("-")) {
                            result = difference(p1.arrlist, p2.arrlist);
                        }
                        System.out.println("Saving subquery: " + "(" + p1.name + " " + op + " " + p2.name + ")");
                        subqueries.put("(" + p1.name + " " + op + " " + p2.name + ")", result);
                    }
                    stack.push(new ParseElement(result, "(" + p1.name + " " + op + " " + p2.name + ")"));

                    String word2 = strings.pop();
                    String word1 = strings.pop();
                    String operator = strings.pop();
                    String resultstring = "(" + word1 + " " + operator + " " + word2 + ")";
                    strings.push(resultstring);
                } else {
                    stack.push(p1);
                    stack.push(p2);
                    checked = true;
                }
            }
        }

        ArrayList<Node> searchresult = stack.pop().arrlist;
        infix = strings.pop();

        //sorting:
        if(searchresult != null) {
            if (sort) {
                infix += lastpart;
                if (sortbypop) {
                    Collections.sort(searchresult, new PopularityComparator());
                } else if (sortbyrel) {
                    Collections.sort(searchresult, new RelevanceComparator());
                }
            }
            if (!asc) {
                Collections.reverse(searchresult);
            }


            System.out.println("More complex order: ");
            for (Node n : searchresult) {
                System.out.println("Document: " + n.attr.document.name + "\t" + "Popularity: " + n.attr.document.popularity + "\t" + "Relevance: " + n.relevance);
            }
            System.out.println("Number of docs found: " + searchresult.size());


            ArrayList<Document> ret = new ArrayList<Document>();
            for (Node n : searchresult) {
                ret.add(n.attr.document);
            }

            return null;
            //return ret;
        } else {
            return null;
        }
    }

    //modified to account for relevance, to be treated as the sum of relevance of a and b
    public ArrayList<Node> intersection(ArrayList<Node> a, ArrayList<Node> b){
        if(a == null || b == null){
            return null;
        }
        ArrayList<Node> ret = new ArrayList<Node>();
        for(Node n : a){
            for(Node n2 : b){
                if(n.attr.document.name.equals(n2.attr.document.name)){
                    ret.add(new Node(n.word, n.attr, n.count, n.relevance + n2.relevance));
                }
            }
        }
        return ret;
    }

    public ArrayList<Node> union(ArrayList<Node> a, ArrayList<Node> b){
        if(a == null){
            return b;
        } else if (b == null){
            return a;
        }
        ArrayList<Node> ret = new ArrayList<Node>();
        ret.addAll(a);
        for(int i = 0; i < b.size(); i++){
            boolean exists = false;
            for(Node n2 : a){
                if(b.get(i).attr.document.name.equals(n2.attr.document.name)){
                    exists = true;
                    ret.remove(i);
                    ret.add(new Node(b.get(i).word, b.get(i).attr, b.get(i).count, b.get(i).relevance + n2.relevance));
                }
            }
            if(!exists){
                ret.add(b.get(i));
            }
        }
        return ret;
    }

    //no change because relevance should stay the same as the first operand.
    public ArrayList<Node> difference(ArrayList<Node> a, ArrayList<Node> b){
        if(a == null){
            return null;
        } else if(b == null){
            return a;
        }
        ArrayList<Node> ret = new ArrayList<Node>();
        ret.addAll(a);
        for(Node n : a){
            for(Node n2 : b){
                if(n.attr.document.name.equals(n2.attr.document.name)){
                    ret.remove(n);
                }
            }
        }
        return ret;
    }

    public class ParseElement{
        ArrayList<Node> arrlist;
        String name;
        String operator;

        public ParseElement(ArrayList<Node> arrlist, String name){
            this.arrlist = arrlist;
            this.name = name;
            operator = null;
        }

        public ParseElement(String operator){
            this.operator = operator;
            arrlist = null;
        }
    }

    public class Node{
        Word word;
        Attributes attr;
        int count = 0;
        double relevance = 0.0;

        public Node(Word word, Attributes attr){
            this.word = word;
            this.attr = attr;
            count = 1;
        }

        public Node(Word word, Attributes attr, int count){
            this.word = word;
            this.attr = attr;
            this.count = count;
        }

        public Node(Word word, Attributes attr, int count, double relevance){
            this.word = word;
            this.attr = attr;
            this.count = count;
            this.relevance = relevance;
        }
    }

    public class RelevanceComparator implements Comparator<Node>{

        public int compare(Node o1, Node o2) {
            if(o1.relevance < o2.relevance){
                return -1;
            } else if(o1.relevance > o2.relevance){
                return 1;
            } else {
                return 0;
            }
        }
    }

    public class PopularityComparator implements Comparator<Node>{

        public int compare(Node o1, Node o2) {
            if(o1.attr.document.popularity < o2.attr.document.popularity){
                return -1;
            } else if(o1.attr.document.popularity > o2.attr.document.popularity){
                return 1;
            } else {
                return 0;
            }
        }
    }
}