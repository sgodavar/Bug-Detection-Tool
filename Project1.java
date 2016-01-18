//#################################
//Code: Project1.java
//Group: 46
//ECE653
//#################################
import java.io.*;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.Map;

//******************************************************************************
//In the program we start with extracting the caller and callee information
//from the call graph and store them in callerarray and calleearray respectively.
//The callerarray has all the callers and callee has all the callee.
//We them map the caller from callerarray as keys and repective index of callee
//from calleearray as values.
//after the map is loaded, we take two callee at a time and check their support in
//whole map. We analyse the support of individual function and pairs for confidence
//calculation. While checking the support for two pairs we calculate the confidence
//simultaniously and check if its greater than given srguments to print bug lines.
//We used Hashset as key and values in map interface. We avoided linkedHash set as it
//creates overhead.
//*******************************************************************************

public class Project1 {
	
    static List<List<Integer>> pairs = new ArrayList<List<Integer>>();//this static list of list holds the pairs for which we calculate confidence

	public static void main(String[] args) throws IOException {
		Project1 b1 = new Project1();
		String[] callerarray = new String[70099];//this array is used to store all the caller information
		String[] calleearray = new String[201329];//stores all the callee of the call graph
		ArrayList<String> caller = new ArrayList<String>();
		Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();//We store the caller and callee information in this static map
		int SUPPORT;
        int CONFIDENCE;   
        
        if (args.length > 1) //we pass in the arguments that were specified in verify.sh 
        {
            SUPPORT = Integer.parseInt(args[1]); 
            CONFIDENCE = Integer.parseInt(args[2]); 
        }
        else//default values for support and confidence
        {
            SUPPORT = 3; 
            CONFIDENCE = 65; 
        }

       
        String line = "";//for each line in call graph
        String Caller = "";//to store the current caller of the line read
        String Callee = "";//to store the current callee of the line read
        BufferedReader bufferReader = null;
        try
        {
        	File path = new File("out.txt");//file containing call graph
    	   bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
    			
    		while ((line = bufferReader.readLine()) != null) 
            {
                int FirstIndex = 0;
                int LastIndex = 0;
                int callerIN = line.indexOf("Call graph node for function:");//gets the index of the specified string
                int calleeIN = line.indexOf("calls function");               
                if (callerIN >= 0) //if caller found
                {
                  //when caller is read,it is stored in the callerarray
                    FirstIndex = line.indexOf("'");    
                    LastIndex = line.lastIndexOf("'");
                    if((FirstIndex>=0) && (LastIndex>=0))
                    Caller = line.substring(FirstIndex+1, LastIndex);//Extract caller
                    Set<Integer> calleetrace = new HashSet<Integer>();
                         int a1 = b1.hashfunctioncaller(Caller, callerarray);
                         callerarray[a1] = Caller;//caller is stored in the caller array using hashfunction
                         caller.add(Caller);
                         map.put(a1, calleetrace); //the hash code is used as key and calleetrace is arraylist for callee for that caller
                         
                }
                else if ((calleeIN >= 0) && (line.indexOf("<0x0>")==-1)) //Checking for callee
                {
                    FirstIndex = line.indexOf("'");
                    LastIndex = line.lastIndexOf("'"); 
                    if((FirstIndex>=0) && (LastIndex>=0))
                    Callee = line.substring(FirstIndex+1, LastIndex);
                    Set<Integer> calleeList = new HashSet<Integer>();
                    if(b1.hashtofindbcallee(Callee, calleearray))//checking if callee is already added to calleearray. If not add to calleearray
                    {
                        int indexNumberCallee= b1.hashtofindcallee(Callee, calleearray);
                        calleeList=map.get(b1.hashtofindcaller(Caller, callerarray));
                        calleeList.add(indexNumberCallee);
                        map.put(b1.hashtofindcaller(Caller, callerarray), calleeList);   
                    }
                    else
                    {
                        int a2 = b1.hashfunctioncallee(Callee, calleearray);
                        calleearray[a2] = Callee;
                        calleeList=map.get(b1.hashtofindcaller(Caller,callerarray));
                        calleeList.add(a2);                      
                        map.put(b1.hashtofindcaller(Caller,callerarray), calleeList);
                      
                    }
                }
                   
                
            }
    			
        }
        
        catch (IOException e) 
        {
           
            System.out.println("Error: " + e.toString());
        }
        finally
        {
        	bufferReader.close();//closing the bufferreader
        }
 
        int key1,key2;
        Set<Integer> finalset1 = map.keySet();//will iterate through each key for calculation
        int w = 0;
   
        for (Integer m : finalset1)
        {
            Set<Integer> getCalleeList = map.get(m);//get the set for the current key
            List<Integer> list = new ArrayList<Integer>(getCalleeList);//storing the set in a list
             
            for(int i=0;i<list.size()-1;i++)//we take two pairs at a time
            {
                int j = i+1;
                while(j<list.size())
                {
                    key1 = list.get(i);
                    key2 = list.get(j);
                    boolean checked = search(key1,key2,(w-1));//to check if confidence is already calculated for the pair

                  if(!checked)	
                    {
                	  float confidence12By1=0;
                      float confidence12By2=0;
                      int count1 = 0 , count2 = 0;
                      Set<Integer> checkset = map.keySet();
                      ArrayList<Integer> bug = new ArrayList<Integer>();
                      ArrayList<Integer> bug2 = new ArrayList<Integer>();
                        pairs.add(new ArrayList<Integer>());
                        pairs.get(w).add(key1);
                        pairs.get(w).add(key2);//adding the two pairs to pairs list
                        w++;
                        float confidence12=0;
                        float confidence1=0;
                        float confidence2=0;
                        
                         
                        //Start calculating confidence by iterating through all keys
                        for(Integer test : checkset)//iterating through the map to find support and confidence of the pair
                        {    
                            Set<Integer> testset = map.get(test);
                            //***************************************
                            //checks the presence of both keys in map
                            //***************************************
                            boolean there1 = testset.contains(key1);
                            boolean there2 = testset.contains(key2);
                            if(there1 && there2)
                            {
                            	
                                confidence12++;
                                confidence1++;//support
                                confidence2++;
                                
                            }
                            else if(there1)
                            {
                                confidence1++;
                                bug.add(test);//to track the missing pair
                                count1++;
                            }
                            else if(there2)
                            {
                                confidence2++;
                                bug2.add(test);
                                count2++;
                            }
                        }
                     
                        count1--;
                        count2--;
                      //Formating the output
                        NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
                        f.setMaximumFractionDigits(2);
                        f.setMinimumFractionDigits(2);
                        //we calculate confidence only if it greater than or equal to given support
                        if(confidence12 >= SUPPORT)
                        {
                            confidence12By1=(confidence12/confidence1)*100;//confidence calculation
                            confidence12By2=(confidence12/confidence2)*100;
                            String[] pairsforprint =new String[2];
                            pairsforprint[0] = calleearray[key1];
                            pairsforprint[1] = calleearray[key2];
                            Arrays.sort(pairsforprint);//sorting the pair
                            if(confidence12By1>=CONFIDENCE)//checking the threshold for confidence
                            {
                                while(count1>=0)//Printing all the missing pairs
                                {	
                                int temp1 = bug.get(count1);
                                System.out.println("bug: "+calleearray[key1]+" in "+callerarray[temp1]+", pair: ("+pairsforprint[0]+", "+pairsforprint[1]+"), support: "+(int)confidence12+", confidence: "+f.format(confidence12By1)+"%");
                                count1--;
                                
                                }
                            }
                           
                            if(confidence12By2>=CONFIDENCE)
                            {
                                while(count2>=0)
                                {
                                	
                                int temp3 = bug2.get(count2);
                                System.out.println("bug: "+calleearray[key2]+" in "+callerarray[temp3]+", pair: ("+pairsforprint[0]+", "+pairsforprint[1]+"), support: "+(int)confidence12+", confidence: "+f.format(confidence12By2)+"%");
                                count2--;
                                
                                }
                            }
                        }
                    }
                    j++;
                }
            }
        }
       
	}

	//*************************************************************
	//calculates the index where caller can be added to callerarray
	//*************************************************************
   
    public int hashfunctioncaller(String string, String[] theArray)
	{
		int arrayIndex = 0;
		int arraySize = 70099;
		
			arrayIndex = string.hashCode();
			
			arrayIndex = Math.abs(arrayIndex % 70099);
		    
		    //System.out.println("Modulus Index= " + arrayIndex + " for value " + string);
		    while (theArray[arrayIndex] != null)
		    {
		    	
		    	
		    	arrayIndex = arrayIndex + 7;
				//System.out.println("Collision Try " + arrayIndex + " Instead");
				arrayIndex %= arraySize;
		    }
		    
		return arrayIndex;
	}
    //*****************************************************
    //Searches if the confidence is calculated for the pair
    //*****************************************************   
    static boolean search(int a,int b,int lists)
    {
        boolean calculated = false;
        int i;
     
        for(i=0;i<=lists;i++)
        {
                if(a==pairs.get(i).get(0) 
                		&& b==pairs.get(i).get(1))
                {
                	calculated = true;
                }
                else if(b==pairs.get(i).get(0) 
                		&& a==pairs.get(i).get(1))
                {
                	calculated = true;
                }
        }

        return calculated;
    }
    
  //*************************************************************
  //calculates the index where callee can be added to calleearray
  //*************************************************************  
    public int hashfunctioncallee(String string, String[] theArray)
	{
		int arrayIndex = 0;
		int arraySize = 201329;
		
			arrayIndex = string.hashCode();
			
			arrayIndex = Math.abs(arrayIndex % 201329);
		    
		    //System.out.println("Modulus Index= " + arrayIndex + " for value " + string);
		    while (theArray[arrayIndex] != null)
		    {
		    	
		    	
		    	arrayIndex = arrayIndex + 7;
				//System.out.println("Collision Try " + arrayIndex + " Instead");
				arrayIndex %= arraySize;
		    }
		    
		return arrayIndex;
	}
    
  //****************************************************
    //returns index of the specified string in callerarray
    //****************************************************
    public int hashtofindcaller(String string, String[] theArray)
	{
		int arrayIndex = 0;
		int arraySize = 70099;
			arrayIndex = string.hashCode();
			
			arrayIndex = Math.abs(arrayIndex % 70099);
			
		    while (theArray[arrayIndex] != null)
		    {
		    	
		    	if (theArray[arrayIndex].equals(string)) {
		    		
		    		
		    		
		    		//System.out.println(string + " was found in index "
		    			                        //+ arrayIndex);
		    		 
		    		return arrayIndex;}
		    	
		    	arrayIndex = arrayIndex + 7;
				//System.out.println("Collision Try " + arrayIndex + " Instead");
				arrayIndex %= arraySize;
		    }
		    
		return 0;
	}
    
  //**********************************************
    //returns true if string is found in calleearray
    //**********************************************
    public boolean hashtofindbcallee(String string, String[] theArray)
	{
		int arrayIndex = 0;
		int arraySize = 201329;
			arrayIndex = string.hashCode();
			
			arrayIndex = Math.abs(arrayIndex % 201329);
			
		    while (theArray[arrayIndex] != null)
		    {
		    	
		    	if (theArray[arrayIndex].equals(string)) {
		    		
		    		
		    		
		    		//System.out.println(string + " was found in index "
		    			                       // + arrayIndex);
		    		 
		    		return true;}
		    	
		    	arrayIndex = arrayIndex + 7;
				//System.out.println("Collision Try " + arrayIndex + " Instead");
				arrayIndex %= arraySize;
		    }
		    
		return false;
	}
  //**************************************************** 
    //returns index of the specified string in calleearray
    //**************************************************** 
    public int hashtofindcallee(String string, String[] theArray)
	{
		int arrayIndex = 0;
		int arraySize = 201329;
			arrayIndex = string.hashCode();
			
			arrayIndex = Math.abs(arrayIndex % 201329);
			
		    while (theArray[arrayIndex] != null)
		    {
		    	
		    	if (theArray[arrayIndex].equals(string)) {
		    		
		    		
		    		
		    		//System.out.println(string + " was found in index "
		    			                        //+ arrayIndex);
		    		 
		    		return arrayIndex;}
		    	
		    	arrayIndex = arrayIndex + 7;
				//System.out.println("Collision Try " + arrayIndex + " Instead");
				arrayIndex %= arraySize;
		    }
		    
		return 0;
	}
    
    
    
    

}
