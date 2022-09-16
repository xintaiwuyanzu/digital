#ifndef _FOFD_PACKAGE_H_
#define _FOFD_PACKAGE_H_

#include "fofd_base.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief 创建一个空包。
 *
 * @param[in] lpwszFileName	指定的文件名称。
 * @return 成功返回包句柄，失败返回NULL。
 *				
 */
FOFD_PACKAGE		FOFD_Package_Create(FOFD_LPCWSTR lpwszFileName);

/**
 * @brief 新建一个文档，并追加至包中。
 *
 * @param[in]	hPackage	包句柄。		
 * @return 成功返回文档句柄，失败返回NULL。
 *				
 */
FOFD_DOCUMENT		FOFD_Package_AddDocument(FOFD_PACKAGE hPackage);

/**
 * @brief 新建一个页，并追加至文档中。
 *
 * @param[in]	hDocument	文档句柄。		
 * @return 成功返回页句柄，失败返回NULL。
 *				
 */
FOFD_PAGE			FOFD_Document_AddPage(FOFD_DOCUMENT hDocument);

/**
 * @brief 设置页面大小
 *
 * @param[in]	hPage		页句柄。
 * @param[in]   rect		指定的页面大小范围
 * @return 成功返回文档句柄，失败返回NULL。
 *				
 */
void				FOFD_Page_SetSize(FOFD_PAGE hPage, OCR_RECT rect);

/**
 * @brief 保存修改。
 *
 * @param[in]	hPackage	包句柄。	
 * @return 成功返回TRUE，失败返回FALSE。
 *				
 */
FOFD_BOOL			FOFD_Package_Save(FOFD_PACKAGE hPackage);

/**
 * @brief 销毁包，并释放内存。
 *
 * @param[in]	hPackage	包句柄。		
 * @return 无。
 *				
 */
void				FOFD_Package_Destroy(FOFD_PACKAGE hPackage);


#ifdef __cplusplus
}
#endif

#endif  //_FOFD_PACKAGE_H_
