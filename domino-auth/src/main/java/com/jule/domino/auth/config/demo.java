package com.jule.domino.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author xujian 2117-12-14
 */
public class demo {
    private final static Logger logger = LoggerFactory.getLogger(demo.class);
    public static void main(String[] args) throws IOException {
        Config.load();
////        File ctoFile = new File(args[0]);
//        File ctoFile = new File("C:\\Users\\xiaok\\Desktop\\name.txt");
//        InputStreamReader rdCto = new InputStreamReader(new FileInputStream(ctoFile));
//        BufferedReader bfReader = new BufferedReader(rdCto);
//        String txtline = null;
//        List<String> list = new ArrayList<>();
//        while ((txtline = bfReader.readLine()) != null) {
//            list.add(txtline);
//        }
//        bfReader.close();
//        List<String> li = new ArrayList<>();
//        for (String item : list) {
//            if (!li.contains(item)) {
//                li.add(item);
//            }
//        }
        //i < 567
//        for(int i = 0;i < 567; i++){
//            System.out.println("1000000000"+i);
//            System.out.println("https://dominohappy.joloplay.net/icon/picture"+((int) (Math.random() * 21) + 1)+".png");
//        }


//        for(int i = 0;i < 567; i++){
//            User user = DBUtil.selectByPrimaryKey("1000000000"+i);
//            if(user != null) {
//                user.setIco_url("https://dominohappy.joloplay.net/icon/picture" + ((int) (Math.random() * 130) + 1) + ".png");
//                int in = DBUtil.updateByPrimaryKey(user);
//                System.out.println(in + " -> update success -> " + i);
//            }
//        }


//        for(int i = 0;i < li.size(); i++){
////            System.out.println(i+":"+li.get(i));
//            User user = new User();
//            user.setId("1000000000"+i);
//            user.setIco_url("");
//            user.setNick_name(li.get(i));
//            user.setMoney(100000l);
//            user.setChannel_id("robot");
//            user.setClient_version("a");
//            user.setDevice_num("a");
//            user.setPlatform(0);
//            user.setUser_ip("");
//            user.setLast_login(new Date());
////            int count = DBUtil.insert(user);
////            System.out.println(li.get(i)+"："+count);
//        }
    }


    //        saveRecordInFile(st);
//}

//    public static  void saveRecordInFile(List<String> list) {
//        File record = new File("C:\\Users\\xiaok\\Desktop\\robot.txt");//记录结果文件
//        try {
//            if (!record.exists()) {
//
//                File dir = new File(record.getParent());
//                dir.mkdirs();
//                record.createNewFile();
//            }
//            FileWriter writer = null;
//            try {
//                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
//                writer = new FileWriter(record, true);
//                for(String str : list){
//                    writer.write(str+"\r\n");
//                }
//            } catch (IOException e) {
//            } finally {
//                try {
//                    if (writer != null) {
//                        writer.close();
//                    }
//                } catch (IOException e) {
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("记录保存失败");
//        }
//    }
}
