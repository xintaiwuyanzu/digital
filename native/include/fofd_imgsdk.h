#ifndef _FOFD_IMGSDK_H_
#define _FOFD_IMGSDK_H_

#include "fofd_base.h"
#include "fofd_package.h"
#include "fofd_ocr.h"

class COCR_IMGPROPERTY {
 public:
  COCR_IMGPROPERTY();
  ~COCR_IMGPROPERTY();
  void reset();
 public:
  int m_nWidth;             //图片宽度,单位毫米
  int m_nHeight;            //图片高度,单位毫米
  int m_nXPixel;            //图片宽度,单位像素
  int m_nYPixel;            //图片宽度,单位像素
  float m_fImgOfdFBLX;      //根据图片横向分辨率计算毫米单位
  float m_fImgOfdFBLY;      //根据图片纵向分辨率计算毫米单位
  float m_fWidthPerPixel;   //每像素的宽度,单位毫米
  float m_fHeightPerPixel;  //每像素的高度,单位毫米

  float m_fTextMoveX;  //图片在页面的起始位置上它的 rect.x
                       //，计算文字位置要加上这个偏移量
  float m_fTextMoveY;  //图片在页面的起始位置上它的 rect.y
                       //，计算文字位置要加上这个偏移量

  int m_nOrientation;	//EXIF格式的JPEG图片，扩展信息Orientation
  FX_OCRHANDLER* m_pHandler;
};

#endif  //_FOFD_IMGSDK_H_
