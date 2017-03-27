package zju.homework.augmentedstudio.Java;

/**
 * Created by 马啸远 on 2017/1/7.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import zju.homework.augmentedstudio.Container.ObjectInfoData;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.GetImageByUrl;

/**
 * 自定义的适配器
 */
public class ImageAdapter extends ArrayAdapter {
    private int resourceID;		//子控件的ID
    private Context mContext;

    private Typeface mTypeface;

    public ImageAdapter(Context context, int viewResourceID, ArrayList<ObjectInfoData> objs) {
        super(context, viewResourceID, objs);
        mContext = context;
        mTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/MONACO.ttf");
        resourceID = viewResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;

        ObjectInfoData obj = (ObjectInfoData)getItem(position);
        String urlString = obj.getImageUrl();

        if(convertView == null) {	//没有缓存
            convertView = LayoutInflater.from(getContext()).inflate(resourceID, null);
            viewHolder = new ViewHolder();

            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.iv_image);
            viewHolder.textView = (TextView)convertView.findViewById(R.id.tv_url);

            convertView.setTag(viewHolder);
        } else {		//有缓存
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();		//从缓存中得到两个视图
        }

        GetImageByUrl getTool = new GetImageByUrl();

        if(urlString != null) {
            getTool.setImage(viewHolder.imageView, urlString);  //设置控件的图片
        }

        viewHolder.textView.setText(getNameByUrl(urlString));   //文字显示从URL中提取的名字
        viewHolder.textView.setTypeface(mTypeface);

        return convertView;
    }

    private final class ViewHolder {		//自定义
        ImageView imageView;
        TextView textView;
    }

    //获取URL最后的图片名字
    private String getNameByUrl(String url) {
        String[] tmp = url.split("\\/");
        return tmp[tmp.length - 1];
    }
}
