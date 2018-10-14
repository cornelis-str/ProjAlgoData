import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;


public class Sort {

    public static ArrayList<TinySearchEngine.DocumentHolder> Bubblesort(ArrayList<TinySearchEngine.DocumentHolder> list, int n) {
        if (list.isEmpty()) {
            throw new NullPointerException("Is your head as empty as this list?");
        }


        ListIterator<TinySearchEngine.DocumentHolder> iter = list.listIterator();
        int R = list.size() - 2;
        boolean swapped = true;
        while (R >= 0 && swapped) {
            swapped = false;
            for (int i = 0; i <= R; i++) {
                if(n == 0) {
                    if (iter.next().attr.occurrence - iter.next().attr.occurrence < 0) {
                        swapped = true;
                        Collections.swap(list, i, i + 1);
                    }
                } else if(n == 1){
                    if (iter.next().count - iter.next().count < 0) {
                        swapped = true;
                        Collections.swap(list, i, i + 1);
                    }
                } else if(n == 2){
                    if (iter.next().attr.document.popularity - iter.next().attr.document.popularity < 0) {
                        swapped = true;
                        Collections.swap(list, i, i + 1);
                    }
                }
                iter.previous();
            }
            iter = list.listIterator();
            R--;
        }
        return list;
    }
}


