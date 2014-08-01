package com.ncfgroup.dbstat.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class URLParser
{
  enum SearchSite{
    baidu("baidu.com"),google_hk("google.com.hk"),google_com("google.com"),soso("soso.com"),sougou("sougou.com"),
    s360("so.360.cn"),yahoo_cn("yahoo.cn"),yahoo_com("yahoo.com"),youdao("youdao.com"),bing("bing"),zhongsou("zhongsou");
    private String value;
    SearchSite(String value){
      this.value = value;
    }
    public String getValue(){
      return value;
    }
  }
  
  enum HomeSite{
    cn9888("9888.cn");
    private String value;
    HomeSite(String value){
      this.value = value;
    }
    public String getValue(){
      return value;
    }
  }
  
  public static String parse(String url){
    if(StringUtil.isNullOrBlank(url)){
      return "1";
    }
    SearchSite[] searchArray = SearchSite.values();
    for(SearchSite site : searchArray){
      if(url.indexOf(site.value) != -1){
        return "0";
      }
    }
    HomeSite[] homeArray = HomeSite.values();
    for(HomeSite site : homeArray){
      if(url.indexOf(site.value) != -1){
        return "2";
      }
    }
    return "2";
  }
  

  public static String unescape(String src)
  {
    StringBuffer tmp = new StringBuffer();
    tmp.ensureCapacity(src.length());
    int lastPos = 0, pos = 0;
    char ch;
    while (lastPos < src.length()) {
      pos = src.indexOf("%", lastPos);
      if (pos == lastPos) {
        if (src.charAt(pos + 1) == 'u') {
          ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
          tmp.append(ch);
          lastPos = pos + 6;
        } else {
          ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
          tmp.append(ch);
          lastPos = pos + 3;
        }
      } else {
        if (pos == -1) {
          tmp.append(src.substring(lastPos));
          lastPos = src.length();
        } else {
          tmp.append(src.substring(lastPos, pos));
          lastPos = pos;
        }
      }
    }
    return tmp.toString();
  }

  public static String getKeyword(String url)
  {
    String keywordReg = "(?:yahoo.+?[\\?|&]p=|openfind.+?query=|google.+?q=|lycos.+?query=|onseek.+?keyword=|search\\.tom.+?word=|search\\.qq\\.com.+?word=|zhongsou\\.com.+?word=|search\\.msn\\.com.+?q=|yisou\\.com.+?p=|sina.+?word=|sina.+?query=|sina.+?_searchkey=|sohu.+?word=|sohu.+?key_word=|sohu.+?query=|163.+?q=|baidu.+?wd=|soso.+?w=|3721\\.com.+?p=|Alltheweb.+?q=)([^&]*)";
    String encodeReg = "^(?:[\\x00-\\x7f]|[\\xfc-\\xff][\\x80-\\xbf]{5}|[\\xf8-\\xfb][\\x80-\\xbf]{4}|[\\xf0-\\xf7][\\x80-\\xbf]{3}|[\\xe0-\\xef][\\x80-\\xbf]{2}|[\\xc0-\\xdf][\\x80-\\xbf])+$";
    Pattern keywordPatt = Pattern.compile(keywordReg);
    StringBuffer keyword = new StringBuffer(20);
    Matcher keywordMat = keywordPatt.matcher(url);
    while (keywordMat.find()) {
      keywordMat.appendReplacement(keyword, "$1");
      System.out.println("===---"+keywordMat.start());
    }
    if (!keyword.toString().equals("")) {
      String keywordsTmp = keyword.toString().replace("http://www.", "");
      Pattern encodePatt = Pattern.compile(encodeReg);
      String unescapeString = unescape(keywordsTmp);
      Matcher encodeMat = encodePatt.matcher(unescapeString);
      String encodeString = "gbk";
      if (encodeMat.matches()){
        encodeString = "utf-8";
      }
      try {
        return URLDecoder.decode(keywordsTmp, encodeString);
      } catch (UnsupportedEncodingException e) {
        return "";
      }
    }
    return "";
  }

  public static void main(String... args)
  {
    String sss = "http://www.baidu.com/s?wd=url%20java%20%E8%A7%A3%E6%9E%90%20%E5%AD%97%E7%AC%A6%E4%B8%B2&pn=100&ie=utf-8&rsv_page=1";
    System.out.println(getKeyword(sss));
  }

}
