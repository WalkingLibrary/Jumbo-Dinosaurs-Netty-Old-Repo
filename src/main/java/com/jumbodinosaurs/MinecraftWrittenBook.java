package com.jumbodinosaurs;


import java.util.ArrayList;
import java.util.List;

public class MinecraftWrittenBook
{
    private String author;
    private String title;
    private String generation;
    private String count;
    private List<String> pages;

    public MinecraftWrittenBook(String author, String title, String generation, String count, List<String> pages)
    {
        this.author = author;
        this.title = title;
        this.generation = generation;
        this.count = count;
        this.pages = pages;
    }

    public String toString()
    {
        String textOfPages = " \n    Page 1 : ";

        for (int i = 0; i < pages.size(); i++)
        {
            if (!(i + 1 < pages.size()))
            {
                textOfPages += pages.get(i);
            }
            else
            {
                textOfPages += pages.get(i) + "\n    Page " + (i + 2) + " : ";
            }

        }

        return "Author: " + author + " Title: " + title + " Generation: " + generation + textOfPages;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getGeneration()
    {
        return this.generation;
    }

    public void setGeneration(String generation)
    {
        this.generation = generation;
    }

    public List<String> getPages()
    {
        return this.pages;
    }

    public int getCount()
    {
        return Integer.valueOf(this.count);
    }

    public void setCount(int count)
    {
        this.count = "" + count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    public boolean hasNulls()
    {
        if(this.author != null)
        {
            if(this.title != null)
            {
                if(this.generation != null)
                {
                    if(this.pages != null)
                    {
                        if(this.count != null)
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isGoodPost()
    {
        if(!this.hasNulls())
        {
            if(this.author.length() < 17)
            {
                return true;
            }
        }
        return false;
    }

    public boolean equals(MinecraftWrittenBook book)
    {
        if (this.author.equals(book.getAuthor()))
        {
            if (this.title.equals(book.getTitle()))
            {
                if (this.generation.equals(book.getGeneration()))
                {
                    ArrayList<String> bookPages, thisClassPages;
                    bookPages = (ArrayList<String>) book.getPages();
                    thisClassPages = (ArrayList<String>) this.pages;
                    if (bookPages.size() == thisClassPages.size())
                    {
                        for (int i = 0; i < thisClassPages.size(); i++)
                        {
                            if (!(thisClassPages.get(i).equals(bookPages.get(i))))
                            {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}