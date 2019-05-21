#  LeakScope
**LeakScope** is a static analysis tool to automatically detect data leakage vulnerabilities in mobile apps. The key component of **LeakScope** is the **String Value Analysis**, which is designed to apply value-set analysis for revealing the possible values of the keys that are used by the cloud provider to connect the apps with the corresponding  back-end instances. This component takes the apk file of the app, the target cloud APIs, and the index of parameters as input and generates the values of the keys that can be solved.

For more details, please see the following [running example](#jump) and [our paper](http://web.cse.ohio-state.edu/~lin.3021/file/SP19.pdf) (S&P 2019)

# Dependencies
This is an Eclipse project that depends on Flowdroid:

- [flowdroid](https://github.com/secure-software-engineering/FlowDroid)
    - [soot-infoflow-android](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow-android "soot-infoflow-android")
    - [soot-infoflow-cmd](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow-cmd "soot-infoflow-cmd")
    - [soot-infoflow-summaries](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow-summaries "soot-infoflow-summaries")
    - [soot-infoflow](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow "soot-infoflow")
# <span id=“jump”>Running Example</span>

### target example code from *example/ValueSetAnalysisExample.apk*
```java
package com.example.vsa.valuesetanalysisexample;  
  
import ...
public class VsaTest {  
  
    String keypart1;  
    String keypart2;  
  
    public void init(Context arg3){  
        keypart1 = getHardcodedStr();  
        keypart2 = arg3.getResources().getString(R.string.key_part2);  
    }  
  
    public String getHardcodedStr(){  
        return "hardcode";  
    }  
  
    public CloudStorageAccount getAccount() throws URISyntaxException, InvalidKeyException {  
        String key = "part1:";  
        key += keypart1;  
        key += "|part2:";  
        key += keypart2; 
         
	//target function
        return CloudStorageAccount.parse(key);  
    }  
}
```
### configuration file
```json
{
  "apk":"example/ValueSetAnalysisExample.apk",
  "methods":
    [
      {
        "method":"<com.microsoft.azure.storage.CloudStorageAccount: com.microsoft.azure.storage.CloudStorageAccount parse(java.lang.String)>",
        "parmIndexs":[0]
      }
    ]
}
```
### run
```sh
$ java -jar ValueSetAnalysis.jar ./libs/android.jar ./example/example.json 
May 20, 2019 8:53:14 PM brut.androlib.res.AndrolibResources loadMainPkg
INFO: Loading resource table...
Using './libs/android.jar' as android.jar
com.example.vsa.valuesetanalysisexample[CG time]:10035
com.example.vsa.valuesetanalysisexample[CG time]:10774
...
com.example.vsa.valuesetanalysisexample===========================875195900===========================
Class: com.example.vsa.valuesetanalysisexample.VsaTest
Method: <com.example.vsa.valuesetanalysisexample.VsaTest: com.microsoft.azure.storage.CloudStorageAccount getAccount()>
Target: $r3 = staticinvoke <com.microsoft.azure.storage.CloudStorageAccount: com.microsoft.azure.storage.CloudStorageAccount parse(java.lang.String)>($r2)
Solved: true
Depend: 422751563, 1674086835, 
BackwardContexts: 
  0
    $r1 = new java.lang.StringBuilder
    specialinvoke $r1.<java.lang.StringBuilder: void <init>()>()
    virtualinvoke $r1.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("part1:")
    $r2 = $r0.<com.example.vsa.valuesetanalysisexample.VsaTest: java.lang.String keypart1>
    virtualinvoke $r1.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r2)
    $r2 = virtualinvoke $r1.<java.lang.StringBuilder: java.lang.String toString()>()
    $r1 = new java.lang.StringBuilder
    specialinvoke $r1.<java.lang.StringBuilder: void <init>()>()
    virtualinvoke $r1.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r2)
    virtualinvoke $r1.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("|part2:")
    $r2 = virtualinvoke $r1.<java.lang.StringBuilder: java.lang.String toString()>()
    $r1 = new java.lang.StringBuilder
    specialinvoke $r1.<java.lang.StringBuilder: void <init>()>()
    virtualinvoke $r1.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r2)
    $r2 = $r0.<com.example.vsa.valuesetanalysisexample.VsaTest: java.lang.String keypart2>
    virtualinvoke $r1.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r2)
    $r2 = virtualinvoke $r1.<java.lang.StringBuilder: java.lang.String toString()>()
    $r3 = staticinvoke <com.microsoft.azure.storage.CloudStorageAccount: com.microsoft.azure.storage.CloudStorageAccount parse(java.lang.String)>($r2)
ValueSet: 
   |0:part1:hardcode|part2:fromres,

com.example.vsa.valuesetanalysisexample===========================422751563===========================
Field: <com.example.vsa.valuesetanalysisexample.VsaTest: java.lang.String keypart2>
Solved: true
Depend: 1568161955, 
ValueSet: 
   |-1:fromres,

com.example.vsa.valuesetanalysisexample===========================1674086835===========================
Field: <com.example.vsa.valuesetanalysisexample.VsaTest: java.lang.String keypart1>
Solved: true
Depend: 38076305, 
ValueSet: 
   |-1:hardcode,

com.example.vsa.valuesetanalysisexample===========================1568161955===========================
Class: com.example.vsa.valuesetanalysisexample.VsaTest
Method: <com.example.vsa.valuesetanalysisexample.VsaTest: void init(android.content.Context)>
Target: $r0.<com.example.vsa.valuesetanalysisexample.VsaTest: java.lang.String keypart2> = $r2
Solved: true
Depend: 
BackwardContexts: 
  0
    $r2 = virtualinvoke $r3.<android.content.res.Resources: java.lang.String getString(int)>(2131427369)
    $r0.<com.example.vsa.valuesetanalysisexample.VsaTest: java.lang.String keypart2> = $r2
ValueSet: 
   |-1:fromres,

com.example.vsa.valuesetanalysisexample[{"0":["part1:hardcode|part2:fromres"]}]

```
# Contact

**LeakScope** was primarily developed by **Chaoshun Zuo** at the Ohio State University, collaborated with [Dr. Yinqian Zhang](http://web.cse.ohio-state.edu/~zhang.834/), and  supervised by [Dr. Zhiqiang Lin](http://web.cse.ohio-state.edu/~lin.3021/).

If you have any question about this project, please contact:

- Chaoshun Zuo( zuo dot 118 at buckeyemail dot osu dot edu)
