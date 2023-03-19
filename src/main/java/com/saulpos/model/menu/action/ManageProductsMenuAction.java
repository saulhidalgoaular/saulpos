package com.saulpos.model.menu.action;

import com.saulpos.model.bean.Product;

public class ManageProductsMenuAction extends CrudMenuAction{

    public ManageProductsMenuAction() {
        super(Product.class);
    }
}
