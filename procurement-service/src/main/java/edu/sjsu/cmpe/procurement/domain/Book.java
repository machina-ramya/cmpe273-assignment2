package edu.sjsu.cmpe.procurement.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class Book {
    private long isbn;
    private String title;
    private String category;

    @JsonProperty
    private String coverimage;
    
    /**
     * @return the isbn
     */
    public long getIsbn() {
	return isbn;
    }

    /**
     * @param isbn
     *            the isbn to set
     */
    public void setIsbn(long isbn) {
	this.isbn = isbn;
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String aCategory) {
        category = aCategory;
    }

    public String getCoverImage() {
        return coverimage;
    }

    public void setCoverImage(String aCoverImage) {
        coverimage = aCoverImage;
    }

    public String format() {
        String delimiter = ":";
        String escapedQuote = "\"";
        String str = isbn + delimiter + 
               escapedQuote + title + escapedQuote + delimiter +
               escapedQuote + category + escapedQuote + delimiter +
               escapedQuote + coverimage + escapedQuote;

        System.out.println(str);
        return str;
    }

    @Override
    public String toString() {
        
        return "Book [ isbn=" + isbn + ", title=" + title + ", category=" 
               + category + ", coverimage=" + coverimage + " ]";
    }
}
