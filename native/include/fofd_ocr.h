#ifndef _FOFD_OCR_H_
#define _FOFD_OCR_H_

#include <string>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif
struct OCR_Font {
  const wchar_t* pwFontName;  //字体名称
  int nFontSize;              //字体大小,单位：毫米
  int nFontColor;             //字体颜色
  FOFD_BOOL bBold;            //是否粗体
  FOFD_BOOL bItalic;          //是否斜体
  FOFD_BOOL bUnderline;       //是否下划线
  float fWordSpace;           //字间距
};

typedef struct _FX_OCRHANDLER {
  int (*OCR_Recognize)(void* client, const wchar_t* pwImgFile);
} FX_OCRHANDLER;

struct IMAG_Attr {
  int m_nWidth;
  int m_nHeight;
  int m_nXDPI;
  int m_nYDPI;
};

/*OCR识别结果数据*/
struct OCR_DATA {
  OCR_RECT rect;  // ocr区域
  char* text;     //字符
  float score;    //可信度（满分1）
};

typedef struct _FX_OCR_RESULT_PROC {
  int (*ocr_result_proc)(OCR_DATA*, int size, void*);
  void* param;
} FX_OCR_RESULT_PROC;

/**
 * @brief 添加图片对象。
 *
 * @param[in]   client    自定义数据。
 * @param[in]   rect      图片外接矩形。单位：毫米
 * @param[in]   pwImgFile 图片文件名，包含路径
 * @param[in]   option    OCR可选参数
 * @param[in]   handler   外部OCR Handler，实现图片OCR识别由外部应用处理
 * @return 成功返回0。
 *
 */
void OFD_OCR_Image(void* client, const OCR_RECT& rect, const wchar_t* pwImgFile,
                   long option = 0, FX_OCRHANDLER* handler = NULL);

/**
 * @brief 添加文字对象。
 *
 * @param[in]   client   自定义数据。
 * @param[in]   pwText   字符串。
 * @param[in]   font     字体信息。
 * @param[in]   rect     字符串外框。单位：毫米, 左上角为原点
 * @param[in]   deltax
 * 字符间偏移（设置NULL，字符间偏移默认rect.width/字符串个数，否则长度为字符串个数-1）。
 * @return 成功返回0。
 *
 */
int OFD_OCR_AddCharInfo(void* client, const wchar_t* pwText, OCR_Font& font,
                        OCR_RECT& rect, float* deltax);

/**
 * @brief OCR识别图片输出到指定格式文件
 * @param[in] pSrcFile   WintoneOCR:png,jpg格式图片,pdf文件;
 *                       KPOCR:png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
 * @param[in] pDestFile  txt、双层PDF、双层OFD, 结构化数据json文件(KPOCR)。
 *                       PDF/OFD输出尺寸大小：如果图片最长边小于A4最长边（297.0）的90%,
 *                       那么就取图片原始尺寸； 其它尺寸按比例缩放为A4大小
 * @param[in] option            ocr扩展选项,目前用于WintoneOCR及KPOCR
 *   WintoneOCR:((单双层)&0x01)|((txt文本字符编码&0x0f)<<1)|(OUTPUT_RTF_OPTION&0x1F)|((语言编码&0x3f)<<5)|(OCRFILE_OPTION&0x03<<11)
 *       bit0: img2pdf选项，单双层，0-双层，1-单层图片，
 *       bit1: img2txt，文本字符编码，具体值参见文通文档，下同
 *       bit0-bit4: img2rtf, OUTPUT_RTF_OPTION
 *       bit5-bit10: 语言编码
 *       bit11-bit12: pdf识别参数,OCRFILE_OPTION
 *   KPOCR:((单双层)&0x1)|((is_img&0x1)<<12)|((use_cls&0x1)<<33)
 *       bit0: 单双层，0-双层，1-单层图片，仅对图片文件有效,pdf/ofd输入直接转双层 
 *       bit12: ofd页面是否完全为img, 0-图文混合，1-完全图片，默认0
 *       bit14: 是否添加分页符，默认0-否，1-添加分页符：“_FXKP_OFD2TXT_PAGE_${NO}_”，${NO}为页码从1开始递增
 *       bit33: 是否使用方向分类器，1-使用，默认0不用
 * @return 正确返回 0， 错误返回其它值
 */
int OFD_OCR_OutFile(const char* pSrcFile, const char* pDestFile,
                   long long option = 0);

/**
 * @brief OCR识别图片输出到指定格式文件。
 * 一个IMGSDK包只会引入一个OCR引擎（目前有KPOCR、WintoneOCR、YINGYUANOCR）
 * @param[in] srcFile    WintoneOCR:png,jpg格式图片,pdf文件;
 *                       KPOCR:png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
 * @param[in] destFile   txt、双层PDF、双层OFD, 结构化数据json文件。
 *                       PDF/OFD输出尺寸大小：如果图片最长边小于A4最长边（297.0）的90%,
 *                       或者指定img_scaling为100, 那么就取图片原始尺寸（DPI正常可计算尺寸）；
 *                       其它按比例缩放为A4大小
 * @param[in] option     可选参数（格式:json），不用的参数就不要给，可为NULL
 *  {
 *    "to_img":0     #OFD/PDF单双层选项, 0-双层，1-单层图片，仅对图片文件有效, pdf/ofd输入直接转双层
 *    "img_scaling":0   #image转OFD（经ofd再pdf也可以）有效, 图片缩放百分比，默认0，自动处理；
 *          100-图像原始尺寸（限dpi正常可计算图像尺寸）
 *    "pages":{"doc_idx":0,"start":0,"count":0}
 *         #输入为多页文件（gif/tiff/tiff/pdf/ofd）时，可选择部分页{起点（从0开始,ofd可指定doc_idx），多少页}输出
 *         #kpocr 同时支持gif,tiff,tif按页选择
 *    "margin":{"left":0,"right":0,"top":0,"bottom":0,}  #img转ofd或pdf时，页边距设置
 *    "txt_encode":0 #文通OCR预留，0-UNICODE，1-GB2312, 2-TXT_BIG5
 *                    3-TXT_SJIS, 4-TXT_KCS, 5-TXT_UTF8, 6-TXT_ISO1252
 *    "rtf_option":0 #文通OCR支持，RTF 文件选项: 0-简单文本 ,1-输出字号 ,2-精确版面还原 ,
 *                     3-类型 mask ,4-去除硬回车
 *    "lang_code":0  #文通OCR支持（kpocr目前是修改配置实现）
 *                     0-简体中文 ,1-繁体中文 ,2-纯英文和数字 ,3-日文 ,
 *                     4-手写体 ,5-简体大字符集 ,6-繁体大字符集 ,7-韩文 ,14-阿拉伯
 *    "ofd_img":0    #kpocr支持，ofd页面是否完全为img, 0-图文混合，1-完全图片，默认0
 *    "page_break":0 #kpocr支持，是否添加分页符，默认0-否，1-添加分页符：“_FXKP_OFD2TXT_PAGE_${NO}_”，${NO}为页码从1开始递增
 *    "use_cls": 0   #kpocr支持,是否使用方向分类器，1-使用，默认0不用
 *  }
 * @return 正确返回 0， 错误返回其它值
 */
int OFD_OCR_OutFileJson(const char* pSrcFile, const char* pDestFile,
                   const char* option = NULL);



/**
 * @brief 获取图片属性
 * @param[in] pImgFile    输入png,jpg格式图片
 * @param[out]pImgAttr    图片属性
 * @return 正确返回 0， 错误返回其它值
 */
int OFD_OCR_GetImgAttr(const char* pImgFile, IMAG_Attr* pImgAttr);

/**
 * @brief OCR识别图片输出json格式数据
 * @param[in]  pImgFile 支持png,jpg,jpeg,bmp,gif,tif,tiff格式图片,pdf/ofd图片文件
 * @param[out] ocr_data json格式数据，内部会自动申请内存，结束后调用OFD_FreeData释放内存。
 * @param[in]  option   扩展选项：KPOCR支持(use_cls&0x1)<<33) 
 *               bit33: KPOCR支持,是否使用方向分类器，1-使用，默认0不用
 * @return 正确返回 0， 错误返回其它值
 */
int OFD_OCR_OutData(const char* pImgFile, char** json_data, long long option);

/**
 * @brief 将多页图片转存为单页多文件（目标文件名为源文件名_${idx}.${format},
 * idx从0开始，如：aa.tif->aa_0.jpg）
 * @param[in]  src_img_file  输入tif/gif格式图片
 * @param[in]  dst_type      目标文件格式,0-jpg,1-jpeg, 2-png, 3-bmp
 * @param[out] dst_img_files 单页img文件，结束需调用OFD_FreeArrayData释放内存
 * @param[out] size          单页文件个数
 * @param[in]  pDstPath      输出文件路径,
 * 默认为空，源文件路径下生成目标文件
 * @return 0-成功，其它-错误
 */
int OFD_MultiPageImg2MultiFile(const char* src_img_file, int dst_type,
                               char** dst_img_files[], int& size,
                               const char* dst_path = NULL);

/**
 * @brief 释放内存。
 * @param[in]   buf           内存块指针
 * @param[in]   size          数组的长度。
 */
void OFD_FreeArrayData(char** buf[], int size);

/**
 * @brief 释放内存。
 * @param[in]   buf           内存块指针
 */
void OFD_FreeData(char** buf);

void* FX_OCR_Image2OFD_Start(const char* pDstFile);
int FX_OCR_Image2OFD_Add(void* handle, const char* pSrcFile,
                         const char* pTempPath);
int FX_OCR_Image2OFD_End(void* handle, bool bOcr = true);

/**
 * @brief OCR识别文件输出到指定格式文件。
 * @param[in] pOfdPageHandle  页面handle，通过OfdDocument.addPage;
 * @param[in] pSrcImage  图片文件;
 * @param[in] pSrcText   对应的txt文件。
 * @return 正确返回 0， 错误返回其它值
 */
int OFD_OCR_OutFileThirdOcrData(void* pOfdPageHandle,const char* pSrcImage,const char* pSrcText);

#ifdef __cplusplus
}
#endif

#endif  //_FOFD_OCR_H_
