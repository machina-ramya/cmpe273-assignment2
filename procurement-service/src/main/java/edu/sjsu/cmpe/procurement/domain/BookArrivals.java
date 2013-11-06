package edu.sjsu.cmpe.procurement.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.codehaus.jackson.annotate.JsonProperty;

public class BookArrivals {

	@JsonProperty
	private List<Book> shipped_books;

	public List<Book> getShippedBooks() {
        return shipped_books;
    }

    public void setShippedBooks(List<Book> aBookList) {
        shipped_books = aBookList;
    } 

	@Override
	public String toString() {
		String result = "shipped_books [\n";
		ListIterator<Book> li = shipped_books.listIterator();

		while(li.hasNext()) {
			Book b = li.next();
			result += "{ " + b.toString() + " }\n";
		}

		result += "]\n";

		return result;
	}
}