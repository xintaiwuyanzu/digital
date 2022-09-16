#ifndef _FOFD_BASE_H_
#define _FOFD_BASE_H_

#ifdef __cplusplus
extern "C" {
#endif

/**布尔变量，应为TURE或FALSE*/
typedef int						FOFD_BOOL;

/**指向8位字节的指针*/
typedef unsigned char *			FOFD_LPBYTE;

/**指向8位字符的指针*/
typedef char *					FOFD_LPSTR;

/**指向宽字符的指针*/
typedef wchar_t*				FOFD_LPWSTR;

/**指向常宽字符的指针*/
typedef const wchar_t*			FOFD_LPCWSTR;

struct OCR_RECT{
    float			x;				//起始点（左上点）x坐标
    float			y;				//起始点（左上点）y坐标
	float			width;			//宽度
	float			height;			//高度
};

/**
 * 句柄定义
 */
/**句柄类型宏定义*/
#define FOFD_DEFINEHANDLE(name)	typedef struct _##name {void* pData;} * name;

/**包句柄，包含多个文档*/
FOFD_DEFINEHANDLE(FOFD_PACKAGE);

/**文档句柄，包含多个页*/
FOFD_DEFINEHANDLE(FOFD_DOCUMENT);

/**文档句柄，包含多个页*/
FOFD_DEFINEHANDLE(FOFD_PAGE);

/**
 * @brief 初始化。
 *
 * @param[in]	license_id		序列号。	
 * @param[in]	license_path	授权文件路径。
 * @return 成功返回0；失败返回-1。	
 *
 */
int			FOFD_Init(FOFD_LPSTR license_id, FOFD_LPSTR license_path);

/**
 * @brief 释放。
 *
 * @return 无。	
 *
 */
void		FOFD_Destroy();

/**
 * @brief 设置动态库路径。
 *
 * @param[in]	pWorkPath		动态库路径。	
 * @return 成功返回0；失败返回-1。	
 *
 */
int			FOFD_SetLibraryPath(FOFD_LPSTR pWorkPath);

/**
 * @brief 获取动态库路径。
 *
 * @param[out]	pWorkPath		动态库路径。
 * @param[out]	nLen			动态库路径长度。
 * @return。	
 *
 */
void		FOFD_GetLibraryPath(FOFD_LPSTR pWorkPath, int& nLen);

#ifdef __cplusplus
}
#endif

#endif  //_FOFD_BASE_H_
