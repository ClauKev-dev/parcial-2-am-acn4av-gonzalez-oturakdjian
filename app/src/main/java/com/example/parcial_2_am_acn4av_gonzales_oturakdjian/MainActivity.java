package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import java.util.Timer;
import java.util.TimerTask;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends BaseActivity {

    private LinearLayout tabHome, tabDescuentos, tabTienda, tabCuadrado, tabMenu;


    private ViewPager viewPager;
    private LinearLayout layoutDots;
    private CarouselAdapter carouselAdapter;
    private TextView tvCartCount;
    private int cartCount = 0;
    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> allProductsList;
    private TextView tvNoResults;
    private ExecutorService executorService;
    private Handler mainHandler;

    private List<String> carouselImageUrls = Arrays.asList(
            "https://images.ctfassets.net/j0994xxhz671/7mZrf2CjapMDYb2lmndwBI/41fc3747041b10de2ebe79a7b7244d8c/Screenshot_2025-06-02_112612.png",
            "https://www.elcomercio.com/wp-content/uploads/2021/10/bayer.jpg",
            "https://www.periodicopublicidad.com/media/lapublicidad/images/2025/04/15/2025041507111790978.jpg"
    );

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
        setupDrawer();
        tvCartCount = findViewById(R.id.tv_cart_count);
        setupCarousel();

        recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));

        tvNoResults = findViewById(R.id.tv_no_results);

        productList = new ArrayList<>();
        allProductsList = new ArrayList<>();

        productAdapter = new ProductAdapter(this, productList, product -> {

            if (product == null) {
                Toast.makeText(MainActivity.this, "Error: Producto no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(MainActivity.this, "Debes iniciar sesión para agregar productos", Toast.LENGTH_LONG).show();
                return;
            }

            android.util.Log.d("MainActivity", "Intentando agregar producto: " + product.getName());
            CarritoManager.agregarProducto(product);

            actualizarCartCount();

            Toast.makeText(MainActivity.this, product.getName() + " agregado al carrito", Toast.LENGTH_SHORT).show();
        });
        recyclerProducts.setAdapter(productAdapter);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(getMainLooper());

        cargarCarritoUsuario();

        loadProductsFromJson();
    }

    private void cargarCarritoUsuario() {
        CarritoManager.cargarCarrito(new CarritoManager.CarritoLoadListener() {
            @Override
            public void onCarritoLoaded(List<Product> carrito) {
                actualizarCartCount();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("MainActivity", "Error al cargar carrito: " + error);
            }
        });
    }

    private void loadProductsFromJson() {
        String jsonUrl = "https://raw.githubusercontent.com/ClauKev-dev/parcial-2-am-acn4av-gonzalez-oturakdjian/main/products.json";
        
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            
            try {
                URL url = new URL(jsonUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    
                    String jsonString = stringBuilder.toString();
                    parseJsonAndUpdateUI(jsonString);
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(MainActivity.this, "Error al cargar desde URL (Código " + responseCode + "). Cargando desde assets...", Toast.LENGTH_SHORT).show();
                    });
                    loadProductsFromAssets();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Error al cargar desde URL. Cargando desde assets...", Toast.LENGTH_SHORT).show();
                });
                loadProductsFromAssets();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    
    private void loadProductsFromAssets() {
        executorService.execute(() -> {
            try {
                InputStream inputStream = getAssets().open("products.json");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();

                String jsonString = new String(buffer, "UTF-8");
                parseJsonAndUpdateUI(jsonString);
            } catch (IOException e) {

                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        });
    }
    
    private void parseJsonAndUpdateUI(String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            List<Product> newProductList = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                Product product = new Product();
                product.setId(jsonObject.optString("id", String.valueOf(i)));
                product.setName(jsonObject.getString("name"));
                product.setPrice(jsonObject.getDouble("price"));
                product.setImageUrl(jsonObject.optString("imageUrl", ""));
                
                newProductList.add(product);
            }

            mainHandler.post(() -> {
                allProductsList.clear();
                allProductsList.addAll(newProductList);
                productList.clear();
                productList.addAll(newProductList);
                productAdapter.notifyDataSetChanged();
            });
        } catch (JSONException e) {
            mainHandler.post(() -> {
                Toast.makeText(MainActivity.this, "Error al parsear JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarCartCount();
    }

    @Override
    protected void setupTopNavigation() {
        super.setupTopNavigation();

        EditText etSearch = findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProducts(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterProducts(String searchQuery) {
        if (allProductsList == null || allProductsList.isEmpty()) {
            if (tvNoResults != null) {
                tvNoResults.setVisibility(View.GONE);
            }
            return;
        }
        
        productList.clear();
        boolean hasSearchQuery = searchQuery != null && !searchQuery.trim().isEmpty();
        
        if (!hasSearchQuery) {
            productList.addAll(allProductsList);
            if (tvNoResults != null) {
                tvNoResults.setVisibility(View.GONE);
            }
        } else {
            String query = searchQuery.toLowerCase().trim();
            for (Product product : allProductsList) {
                if (product.getName() != null && product.getName().toLowerCase().contains(query)) {
                    productList.add(product);
                }
            }

            if (tvNoResults != null) {
                if (productList.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                } else {
                    tvNoResults.setVisibility(View.GONE);
                }
            }
        }
        
        productAdapter.notifyDataSetChanged();
    }

    public void clearSearch() {
        EditText etSearch = findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.setText("");
        }
        filterProducts("");
    }

    private void setupCarousel() {

        viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);


        carouselAdapter = new CarouselAdapter(this, carouselImageUrls);
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

        ImageView[] dots = new ImageView[carouselImageUrls.size()];

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
                if (currentPage == carouselImageUrls.size() - 1) {
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
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void actualizarCartCount() {
        int total = CarritoManager.getTotalItems();

        if (total > 0) {
            tvCartCount.setText(String.valueOf(total));
            tvCartCount.setVisibility(View.VISIBLE);
        } else {
            tvCartCount.setVisibility(View.GONE);
        }
    }

}