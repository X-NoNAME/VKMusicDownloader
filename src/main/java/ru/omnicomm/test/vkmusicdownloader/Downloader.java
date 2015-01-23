/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.omnicomm.test.vkmusicdownloader;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 *
 * @author Ivan (X-NoNAME) Kazakov
 * @mailto mail@x-noname.ru
 */
class Downloader {
    
    final MainFrame frame;

    Downloader(MainFrame frame) {
        this.frame = frame;
    }

    void start() {
        try {
            Future<List<AudioFile>> future =  Executors.newSingleThreadExecutor().submit(new MyRunnable());
            List<AudioFile> list = future.get();
            ExecutorService es = Executors.newFixedThreadPool(10);
            File folder = new File(frame.jTextField2.getText());
            if(!folder.exists()){
                folder.mkdirs();
            }
            List<Future<AudioFile>> results = new ArrayList<>();
            for(AudioFile aFile: list){
                Future<AudioFile> status = es.submit(new FileDownloader(aFile));
                results.add(status);
            }
            
            for(Future<AudioFile> ft:results){
                AudioFile f;
                try {
                    f = ft.get(120, TimeUnit.SECONDS);
                    if(f.isStatus()){
                        frame.jTextArea1.append("Downloaded '"+f.name+"'\n");
                    }else {
                        frame.jTextArea1.append("!!! Not downloaded '"+f.name+"'\n");
                    }
                } catch (TimeoutException ex) {
                    Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
           
        } catch (InterruptedException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class FileDownloader implements Callable<AudioFile> {

        final AudioFile aFile;
        
        public FileDownloader(AudioFile aFile) {
            this.aFile=aFile;
        }

        @Override
        public AudioFile call() throws Exception {
            String filename = clean(aFile.name)+".mp3";
            try{
                FileUtils.copyURLToFile(new URL(aFile.link),new File(frame.jTextField2.getText(), filename));
                aFile.setStatus(true);
            }catch(Throwable t){
                System.out.println("Error with link "+aFile.link);
            };
            return aFile;
        }

        private String clean(String i_file) {
             String retVal = i_file.replaceAll("/", "");
              retVal = retVal.replaceAll("\\\\", "");
              retVal = retVal.replaceAll(":", "");
              retVal = retVal.replaceAll("\\*", "");
              retVal = retVal.replaceAll("\\?", "");
              retVal = retVal.replaceAll("\"", "");
              retVal = retVal.replaceAll("<", "");
              retVal = retVal.replaceAll(">", "");
              retVal = retVal.replaceAll("|", "");
              retVal = retVal.split("\n")[0];
              return retVal;              
        }
    }

    private class MyRunnable implements Callable<List<AudioFile>> {

        public MyRunnable() {
        }

  

        @Override
        public List<AudioFile> call() throws Exception {
            List<AudioFile> urls = new ArrayList<>();
                WebDriver driver = new FirefoxDriver();
                driver.manage().window().setSize(new Dimension(300, 300));
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
                driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
                driver.get("http://vk.com");
                
                driver.findElement(By.name("email")).sendKeys(frame.jTextField1.getText());
                driver.findElement(By.name("pass")).sendKeys(new String(frame.jPasswordField1.getPassword()));
                
                driver.findElement(By.id("quick_login_button")).click();
                
                driver.findElement(By.linkText("Мои Аудиозаписи")).click();
                System.out.println( ">>>"+driver.findElements(By.cssSelector("input[type='hidden']")));
                
                List<WebElement> divs = driver.findElements(By.xpath("//input[@type='hidden']"));
                
                for(WebElement div:divs){
                    System.out.println("div = "+div);
                    String name = div.findElement(By.xpath("..")).findElement(By.xpath("..")).getText();
                    //String link = (String) ((JavascriptExecutor)driver).executeScript("return arguments[0].getElementsByTagName(\"input\")[0].value", div);
                    String link = div.getAttribute("value").split(",")[0];
                    
                    urls.add(new AudioFile(name,link));
                }
                driver.quit();

            return urls;
        }
        
    }

}
