package com.foxit.img;

public class GetColorMoments {
	
	/* static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
	 public static void main(String[] args) {
		
	    	String imageDir="/home/zhong/Desktop/testpicture/202.jpg";
	    	double [] color=colorMoments(imageDir);
	    	for(int i=0;i<color.length;i++){
	    		System.out.println(color[i]);
	    	}
		
	    	System.out.println("done!!!");			 		 
	 }
	 
	 
	 
	
	 static double[] colorMoments(String path){
		 double [] y = new double[9];
		 Mat imagemat = Highgui.imread(path);
		 double [] B=new double[imagemat.rows()*imagemat.cols()];
		 double [] G=new double[imagemat.rows()*imagemat.cols()];
		 double [] R=new double[imagemat.rows()*imagemat.cols()];
		 for(int j=0;j<imagemat.rows();j++){		
 			for(int k=0;k<imagemat.cols();k++){			
 				double [] data=imagemat.get(j, k);
 				B[j*imagemat.cols()+k]=data[0];
 				G[j*imagemat.cols()+k]=data[1];
 				R[j*imagemat.cols()+k]=data[2];				 
 			}
 		}
		 y[0]=mean(R);
		 y[1]=std(R, mean(R));
		 y[2]=skew(R, mean(R));
		 y[3]=mean(G);
		 y[4]=std(G, mean(G));
		 y[5]=skew(G, mean(G));
		 y[6]=mean(B);
		 y[7]=std(B, mean(B));
		 y[8]=skew(B, mean(B));
		 return y;
		 
	 }
	 static double mean(double [] data){        //一阶矩均值
		 double sum=0;
		 for(int i=0;i<data.length;i++){
			 sum+=data[i];
		 }
		 double mean=sum/data.length;
//		 System.out.println(mean);
		 return mean;
	 }
	 static double std(double [] data,double mean){    //二阶矩方差
		 double sum=0;
		 for(int i=0;i<data.length;i++){
			 sum+=Math.pow((data[i]-mean), 2);
		 }
//		 System.out.println(sum);
		 double std=Math.pow((sum/data.length),0.5);
		 return std;
	 }
	 static double skew(double [] data,double mean){   //三阶矩斜度
		 double sum=0;
		 for(int i=0;i<data.length;i++){
			 sum+=Math.pow((data[i]-mean), 3);
		 }
		 double skew=Math.cbrt(sum/data.length);
		 return skew;
	 }*/

}
