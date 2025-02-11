package com.marinov.instagram;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityManager;
import android.content.Context;
import java.util.Arrays;
import java.util.List;
import android.webkit.CookieManager; // Importar CookieManager

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    // Lista de domínios aprovados
    private static final List<String> APPROVED_DOMAINS = Arrays.asList(
            "instagram.com",
            "help.instagram.com",
            "aistudio.instagram.com",
            "www.instagram.com",
            "ig.me",
            "instagr.am",
            "familycenter.instagram.com",
            "m.instagram.com"
            // Adicione outros domínios aprovados aqui, se necessário
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o WebView
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new CustomWebViewClient()); // Usa um WebViewClient personalizado

        // Configura as opções do WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Ativa JavaScript
        webSettings.setDomStorageEnabled(true); // Habilita o armazenamento local, essencial para cookies
        webSettings.setSupportZoom(true); // Habilita o zoom, se necessário
        webSettings.setBuiltInZoomControls(true); // Exibe controles de zoom
        webSettings.setDisplayZoomControls(false); // Oculta os controles de zoom padrão

        // Habilitar Cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true); // Aceitar cookies de primeiro domínio
        cookieManager.setAcceptThirdPartyCookies(webView, true); // Aceitar cookies de terceiros

        // Verifica se há um link passado para o aplicativo
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                webView.loadUrl(data.toString()); // Carrega a URL recebida no WebView
            }
        } else {
            // Carrega a URL padrão no WebView
            webView.loadUrl("https://www.instagram.com/"); // Substitua pela URL desejada
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Verifica se a URL está na lista de domínios aprovados
            if (isDomainApproved(url)) {
                // Carrega a URL diretamente no WebView
                view.loadUrl(url);
            } else {
                // Para esquemas desconhecidos, tenta abrir no navegador ou aplicativo apropriado
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent); // Tenta abrir o link
            }
            return true; // Indica que a URL foi tratada
        }

        private boolean isDomainApproved(String url) {
            Uri uri = Uri.parse(url);
            String host = uri.getHost(); // Obtém o host da URL

            // Verifica se o domínio está na lista de aprovados
            for (String approvedDomain : APPROVED_DOMAINS) {
                if (host != null && host.contains(approvedDomain)) {
                    return true; // Domínio aprovado
                }
            }
            return false; // Domínio não aprovado
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack(); // Volta para a página anterior no WebView
        } else {
            super.onBackPressed(); // Se não houver páginas anteriores, sai da atividade
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Verifica se o aplicativo está em segundo plano
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null && am.getRunningAppProcesses() != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : am.getRunningAppProcesses()) {
                if (processInfo.processName.equals(getPackageName()) &&
                        processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    finishAffinity();  // Fecha o app completamente
                }
            }
        }
    }
}
