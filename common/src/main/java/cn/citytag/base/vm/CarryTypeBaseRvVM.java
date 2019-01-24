package cn.citytag.base.vm;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.OnListBinding;

/**
 * 作者：Lgc
 * 创建时间：2018/8/24
 * 更改时间：2018/8/24
 */
public class CarryTypeBaseRvVM<T extends OnListBinding> extends ListVM {
    public final LoggingRecyclerViewAdapter<T> adapter = new LoggingRecyclerViewAdapter<>();

    public final ObservableList<T> items = new ObservableArrayList<>();

    //public DiffObservableList<T> diffItems = new DiffObservableList<>();

    //public final MergeObservableList<Object> headerFooterItems = new MergeObservableList<>()
    //		.insertItem(new SearchListVM(null, "",activity))
    //		.insertList(items);

    //public final ItemBinding<MyFansListVm> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_my_fans);

    //public final OnItemBindClass<Object> multipleBinding = new OnItemBindClass<>()
    //		.map(SearchListVM.class, BR.viewModel, R.layout.item_search)
    //		.map(MyFansListVm.class, BR.viewModel, R.layout.item_my_fans);

    public final BindingRecyclerViewAdapter.ItemIds<Object> itemIds = new BindingRecyclerViewAdapter.ItemIds<Object>() {

        @Override
        public long getItemId(int position, Object item) {
            return position;
        }
    };

    public final BindingRecyclerViewAdapter.ViewHolderFactory viewHolderFactory = new BindingRecyclerViewAdapter.ViewHolderFactory() {
        @Override
        public RecyclerView.ViewHolder createViewHolder(ViewDataBinding binding) {
            return new CustomViewHolder(binding.getRoot());
        }
    };

    private static class CustomViewHolder extends RecyclerView.ViewHolder {
        public CustomViewHolder(View itemView) {
            super(itemView);
        }
    }
}
