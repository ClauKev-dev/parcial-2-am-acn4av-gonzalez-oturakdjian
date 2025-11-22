package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import java.util.Timer;
import java.util.TimerTask;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends BaseActivity {

    private LinearLayout tabHome, tabDescuentos, tabTienda, tabCuadrado, tabMenu;


    private ViewPager viewPager;
    private LinearLayout layoutDots;
    private CarouselAdapter carouselAdapter;
    private TextView tvCartCount;
    private int cartCount = 0;


    private int[] images = {
            R.drawable.image1,
            R.drawable.image2
    };

    private int currentPage = 0;
    private Timer timer;
    private final long DELAY_MS = 500;
    private final long PERIOD_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigation();
        setupTopNavigation();
        tvCartCount = findViewById(R.id.tv_cart_count);
        setupCarousel();

        RecyclerView recyclerProducts = findViewById(R.id.recyclerProducts);


        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));


        List<Product> productList = new ArrayList<>();
        productList.add(new Product(R.drawable.image3, "Curitas", 3500.0));
        productList.add(new Product(R.drawable.image4, "Jarabe para la tos", 4200.0));
        productList.add(new Product(R.drawable.image5, "Tafirol para espasmos", 8000.0));
        productList.add(new Product(R.drawable.image6, "Ibuprofeno 400", 6000.0));

        ProductAdapter adapter = new ProductAdapter(this, productList, product -> {

            CarritoManager.agregarProducto(product);


            actualizarCartCount();
        });
        recyclerProducts.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarCartCount();
    }

    private void setupCarousel() {

        viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);


        carouselAdapter = new CarouselAdapter(this, images);
        viewPager.setAdapter(carouselAdapter);


        createDots();


        setupAutoSlide();


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateDots();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }



    private void createDots() {

        layoutDots.removeAllViews();

        ImageView[] dots = new ImageView[images.length];

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    20,
                    20
            );
            params.setMargins(8, 0, 8, 0);
            dots[i].setLayoutParams(params);

            layoutDots.addView(dots[i]);
        }

        updateDots();
    }

    private void updateDots() {
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            ImageView dot = (ImageView) layoutDots.getChildAt(i);


            if (i == currentPage) {
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }
        }
    }

    private void setupAutoSlide() {
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            public void run() {
                if (currentPage == images.length - 1) {
                    currentPage = 0;
                } else {
                    currentPage++;
                }
                viewPager.setCurrentItem(currentPage, true);
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    private void actualizarCartCount() {
        int total = 0;
        for (Product p : CarritoManager.getCarrito()) {
            total += p.getQuantity();
        }

        if (total > 0) {
            tvCartCount.setText(String.valueOf(total));
            tvCartCount.setVisibility(View.VISIBLE);
        } else {
            tvCartCount.setVisibility(View.GONE);
        }
    }

}