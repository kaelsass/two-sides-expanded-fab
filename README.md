在开发OCRus的照相页面时，我们希望能更好的引导用户进行操作，用户在点击照相按钮后，分别向两边弹出确认和删除按钮，并隐藏照相按钮，避免让用户迷惑。最后实现效果如图：
![two sides expanded floating action bar](http://img.blog.csdn.net/20150714125335604)

弹出效果是基于[FloatingActionButton][1]做的，但这个库中子按钮的弹出方向都只能沿同一个方向，都向左、都向右、都向上或都向下，不能分别向左和向右的效果。所以我Hack进源码对这个库做了扩展，源码和示例程序见[Github](https://github.com/kaelsass/two-sides-expanded-fab)。

本文详细介绍对原库关键点的修改过程，大家可根据自己的需求参考修改。

## 项目结构
原库中主要包含四个Java文件：
![structure](http://img.blog.csdn.net/20150714135057748)

- AddFloatingActionButtion是原库中的“+”FAB，点击时会弹出子FAB，并且“+”旋转（90+45）° 变成“×”，再点击“×”子FAB收回，AddFloatingActionButtion旋转回“+”。icon不可修改。
- FloatingActionButton是子FAB，点击AddFloatingActionButtion时弹出，icon可设置。
- FloatingActionsMenu封装AddFloatingActionButtion和FloatingActionButton，控制按钮显示布局，添加监听器并在点击时显示动画。

## 修改点
### attrs.xml
在[android-floating-action-button/src/main/res/values/attrs.xml][6]中可以看到：
``` xml
<attr name="fab_expandDirection" format="enum">
    <enum name="up" value="0"/>
    <enum name="down" value="1"/>
    <enum name="left" value="2"/>
    <enum name="right" value="3"/>
</attr>
```
即子FAB的弹出方向只有向上、向下、向左、向右四种，所有子FAB的弹出方向都相同，在此我们需要扩展一种分别向左和向右弹出的方式：
``` xml
<attr name="fab_expandDirection" format="enum">
    <enum name="up" value="0"/>
    <enum name="down" value="1"/>
    <enum name="left" value="2"/>
    <enum name="right" value="3"/>
    <enum name="left_right" value="4"/>
</attr>
```

### FloatingActionsMenu.java
FloatingActionsMenu的构造函数（init方法）中会读取`fab_expandDirection`的值：
``` java
mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
```
我们在FloatingActionsMenu类中加入final静态变量：
``` java
public static final int EXPAND_LEFT_RIGHT = 4;
```
来标识分别向左右弹出的属性。

onMeasure方法测量当前View所占大小，在测算长度和宽度时，分别向左右弹出与全向左或全向右弹出所占的长度与宽度是相同的，所以对这三种情况用相同的测量方式即可。修改详见[FloatingActionsMenu.java][4]

onLayout方法确定View及子View在父View中的摆放位置。对分别向左右弹出的View，首先放置最中间的FAB：
``` java
addButtonX = (r - l - mAddButton.getMeasuredWidth())/2;	//左边界
addButtonTop = b - t - mMaxButtonHeight + (mMaxButtonHeight - mAddButton.getMeasuredHeight()) / 2;	//上边界
mAddButton.layout(addButtonX, addButtonTop, addButtonX + mAddButton.getMeasuredWidth(), addButtonTop + mAddButton.getMeasuredHeight());	//将主FAB放置在以左上角（addButtonX, addButtonTop）、右下角（addButtonX + mAddButton.getMeasuredWidth(), addButtonTop + mAddButton.getMeasuredHeight()）组成的矩形区域内
```
对每个子FAB，需要根据此子FAB是向左弹出还是向右弹出确定其摆放位置：
``` java
expandLeft = true;	//标识子FAB是向左弹出还是向右弹出

int nextXLeft = addButtonX - mButtonSpacing;	//主FAB左边界 - 按钮间隔
int nextXRight = addButtonX + mAddButton.getMeasuredWidth() + mButtonSpacing;	//主FAB左边界 + 主FAB大小 + 按钮间隔

int childX = expandLeft ? nextXLeft - child.getMeasuredWidth() : nextXRight;	//子FAB的左边界
int childY = addButtonTop + (mAddButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;	//子FAB的上边界
child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());	//将子FAB放置在以左上角（childX, childY）和右下角（childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight()）组成的矩形区域内
```
对子FAB需要根据弹出/收回状态确定其位置及动画效果：
``` java
float collapsedTranslation = addButtonX - childX;	//收回状态时，X轴的偏移量
float expandedTranslation = 0f;	//展开状态时，X轴的偏移量

child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);	//如果是弹出状态，则不偏移；否则，偏移量为collapsedTranslation
child.setAlpha(mExpanded ? 1f : 0f);	//如果为弹出状态，显示子FAB；否则，不显示

LayoutParams params = (LayoutParams) child.getLayoutParams();
params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
params.setAnimationsTarget(child);	//设置子FAB弹出与收回时的动画效果
```
当前子FAB位置设置完后，需要更新下一个子FAB的摆放位置：
``` java
nextXLeft = expandLeft ? childX - mButtonSpacing : nextXLeft;
nextXRight = expandLeft ? nextXRight: childX + child.getMeasuredWidth() + mButtonSpacing;
expandLeft = !expandLeft;	//一左一右摆放
```
修改详见[FloatingActionsMenu.java][5]

另外，这个库中的主FAB的icon不可修改，即“+”按钮。为了实现文章一开始gif图中的效果，我对此进行了扩展，在[attrs.xml][2]中的`FloatingActionsMenu`中加入了属性声明：
``` xml
<attr name="fab_menu_icon" format="reference"/>
```
并且在FloatingActionsMenu构造函数（init方法）中读取此属性并判断：如果无此属性，则创建默认的“+”按钮；否则，以此属性引用的图片作为icon创建按钮。
``` java
mIcon = attr.getDrawable(R.styleable.FloatingActionsMenu_fab_menu_icon);
attr.recycle();

if(mIcon == null)	//判断是否有fab_menu_icon属性
    createAddButton(context);
else
    createNormalButton(context);
```
修改详见[FloatingActionsMenu.java][7]

还有一些细节方面的修改，在此不再赘述，如有兴趣请参见[示例](https://github.com/kaelsass/two-sides-expanded-fab)。


[1]: https://github.com/futuresimple/android-floating-action-button
[2]: https://github.com/kaelsass/two-sides-expanded-fab/blob/master/android-floating-action-button/src/main/res/values/attrs.xml
[3]: https://github.com/futuresimple/android-floating-action-button/blob/master/library/src/main/java/com/getbase/floatingactionbutton/FloatingActionsMenu.java
[4]: https://github.com/kaelsass/two-sides-expanded-fab/blob/master/android-floating-action-button/src/main/java/com/getbase/floatingactionbutton/FloatingActionsMenu.java#L261
[5]: https://github.com/kaelsass/two-sides-expanded-fab/blob/master/android-floating-action-button/src/main/java/com/getbase/floatingactionbutton/FloatingActionsMenu.java#L328
[6]: https://github.com/futuresimple/android-floating-action-button/blob/master/library/src/main/res/values/attrs.xml
[7]: https://github.com/kaelsass/two-sides-expanded-fab/blob/master/android-floating-action-button/src/main/java/com/getbase/floatingactionbutton/FloatingActionsMenu.java
