package com.example.nhom4_duan_1.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom4_duan_1.MainActivity;
import com.example.nhom4_duan_1.R;
import com.example.nhom4_duan_1.adapters.CartAdapter;
import com.example.nhom4_duan_1.adapters.OderAdapter;
import com.example.nhom4_duan_1.managers.Manager;
import com.example.nhom4_duan_1.models.Bills;
import com.example.nhom4_duan_1.models.Cart;
import com.example.nhom4_duan_1.models.Products;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class CartActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerCart;
    ArrayList<Products> listPro;
    ArrayList<Cart> listCart;
    AlertDialog alertDialog;
    ArrayList<Products> listTemp;
    Button btnBuy;
    TextView tvSubtotal,tvFee,tvPriceVoucher,tvTotalCart,tvPaymentMethods;
    double subtotal = 0;
    double totalCart = 0;
    double Fee = 0;
    double vouchers = 0;
    String IdUser;
    int amount;
    int pay = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Intent intent = getIntent();
        IdUser = intent.getStringExtra("Id");

        listPro = new ArrayList<>();
        listCart = new ArrayList<>();
        listTemp = new ArrayList<>();


        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvFee = findViewById(R.id.tvFee);
        tvPriceVoucher = findViewById(R.id.tvPriceVoucher);
        tvTotalCart = findViewById(R.id.tvTotalCart);
        btnBuy = findViewById(R.id.btnBuy);
        tvPaymentMethods = findViewById(R.id.tvPaymentMethods);


        recyclerCart = (RecyclerView) findViewById(R.id.recyclerCart);
        ImageView ivBackCart = findViewById(R.id.ivBackCart);
        ivBackCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToBill();
            }
        });

        tvPaymentMethods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPayment();
            }
        });

        getDataProduct();
    }

    public void getPayment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pay, null);
        LinearLayout lnCredit = view.findViewById(R.id.lnCredit);
        LinearLayout lnMomo = view.findViewById(R.id.lnMomo);
        LinearLayout lnCash = view.findViewById(R.id.lnCash);

        lnCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPaymentMethods.setText("Credit or debit card");
                pay =1;
                alertDialog.dismiss();
            }
        });

        lnMomo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPaymentMethods.setText("Momo E-Wallet");
                pay =1;
                alertDialog.dismiss();
            }
        });

        lnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPaymentMethods.setText("Cash");
                pay =1;
                alertDialog.dismiss();
            }
        });

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void getDataCart() {
        listCart.clear();
        db.collection("Cart")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                i += 1;
                                Map<String, Object> item = document.getData();
                                Cart cart = new Cart();
                                cart.setId(document.getId());
                                cart.setId_Product(item.get("Id_Product").toString());
                                cart.setAmount(Integer.parseInt(item.get("Amount").toString()));
                                cart.setTotal(Double.parseDouble(item.get("Total").toString()));
                                listCart.add(cart);
//                                System.out.println(i + " ---" + list.get(list.size() - 1));
                            }
                            getDataCartProduct();
                        } else {
                            Log.w(">>>TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getDataProduct(){
        listPro.clear();
        db.collection("Products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> item = document.getData();
                                Products products = new Products();
                                products.setId(document.getId());
                                products.setName(item.get("Name").toString());
                                products.setImage(item.get("Image").toString());
                                products.setType(item.get("Type").toString());
                                products.setPrice(Double.parseDouble(item.get("Price").toString()));
                                listPro.add(products);
                            }
                            getDataCart();
                        } else {
                            Log.w(">>>TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getDataCartProduct(){
        listTemp.clear();
        for (Products lst1: listPro) {
            for (Cart lst2: listCart) {
                if (lst1.getId().equals(lst2.getId_Product())){
                    listTemp.add(lst1);
                }
            }
        }
        if (listTemp.size() != 0){
            System.out.println( "halo Data Cart " +listTemp.size());
            FillData();
        }
        else {
            setContentView(R.layout.activity_cart_without_products);
            ImageView ivBackOderStart = findViewById(R.id.ivBackOderStart);
            ivBackOderStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            Button btnStarShoping = findViewById(R.id.btnStarShoping);
            btnStarShoping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }

    public void deleteCart(String id){
        db.collection("Cart")
                .document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CartActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                        Fee = 0;
                        vouchers = 0;
                        subtotal= 0;
                        totalCart = 0;
                        getDataProduct();
                    }
                });
    }

    public void updateCartChangAmount(String id, String idPro, int amt){
        for (Products lst: listPro) {
            if (lst.getId().equals(idPro)){
                Map<String, Object> user = new HashMap<>();
                user.put("Id_Product", idPro);
                user.put("Amount", amt);
                user.put("Total", lst.getPrice() * amt);
                System.out.println("Amt: " +amt);
                System.out.println("Total: " +lst.getPrice() * amt);

                // Add a new document with a generated ID
                db.collection("Cart")
                        .document(id)
                        .update(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                System.out.println("Sửa thành công");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Sửa không thành công");
                            }
                        });
            }
        }
        getDataProduct();
    }


    public void getPriceCart(){
        subtotal =0;
        for (Cart lst: listCart) {
            subtotal += lst.getTotal();
            amount += lst.getAmount();
        }
        tvSubtotal.setText(subtotal + "đ");
        System.out.println("Subtotal: " + subtotal);
        if (subtotal > 0){
            Fee = 30000;
            vouchers = -10000;
            tvFee.setText(Fee + "đ");
            tvPriceVoucher.setText(vouchers + "đ");
            totalCart = subtotal + Fee + vouchers;
            tvTotalCart.setText(totalCart + "đ");
        }else {
            setContentView(R.layout.activity_cart_without_products);
            ImageView ivBackOderStart = findViewById(R.id.ivBackOderStart);
            ivBackOderStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            Button btnStarShoping = findViewById(R.id.btnStarShoping);
            btnStarShoping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }
    public String getCalender(){
        DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dfDate.format(Calendar.getInstance().getTime());
    }

    public void addToBill(){
        if (totalCart > 0){
            if (pay != 0){
                Map<String, Object> user = new HashMap<>();
                user.put("Id_User", IdUser);
                user.put("Time", getCalender());
                user.put("Total", totalCart);
                user.put("Amount", amount);

                db.collection("Bills")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                System.out.println("Thêm Bills thành công");

                                String idBills = documentReference.getId();
                                for (Cart lst: listCart) {
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Id_Bill", idBills);
                                    user.put("Id_Product", lst.getId_Product());
                                    user.put("Quantity", lst.getAmount());
                                    user.put("Amount", lst.getTotal());

                                    db.collection("DetailBill")
                                            .add(user)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                    System.out.println("Successful Insert");

                                                    db.collection("Cart")
                                                            .document(lst.getId())
                                                            .delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    getDataProduct();
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println("Lỗi thêm DetailBills");
                                                }
                                            });
                                }
                                Toast.makeText(CartActivity.this, "Buy Successful", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Lỗi thêm Bills");
                            }
                        });
            }
            else {
                getPayment();
            }
        }else {
            Toast.makeText(this, "No Product", Toast.LENGTH_SHORT).show();
        }
    }

    public void FillData() {
        recyclerCart = (RecyclerView) findViewById(R.id.recyclerCart);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CartActivity.this);
        recyclerCart.setLayoutManager(linearLayoutManager);
        System.out.println("ListPro: " + listPro.size());
        CartAdapter adapter = new CartAdapter(CartActivity.this, listCart,listTemp);
        recyclerCart.setAdapter(adapter);
        getPriceCart();
    }


}
