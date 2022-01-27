package dev.westernpine.lib.util;

import dev.westernpine.bettertry.Try;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;

public class ImageCrawler {

    public static String findURL(String url) {
        String contentType = Try.to(() -> new URL(url).openConnection().getContentType()).getUnchecked();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return url;
            }
        }

        String imageUrl = null;

        try {
            Document document = Jsoup.connect(url).get();

            imageUrl = getImageFromSchema(document);
            if (imageUrl != null) {
                return imageUrl;
            }

            imageUrl = getImageFromOpenGraph(document);
            if (imageUrl != null) {
                return imageUrl;
            }

            imageUrl = getImageFromTwitterCard(document);
            if (imageUrl != null) {
                return imageUrl;
            }

            imageUrl = getImageFromTwitterShared(document);
            if (imageUrl != null) {
                return imageUrl;
            }

            imageUrl = getImageFromLinkRel(document);
            if (imageUrl != null) {
                return imageUrl;
            }
        } catch (Exception e) {
        }

        return imageUrl;
    }

    private static String getImageFromTwitterShared(Document document) {
        Element div = document.select("div.media-gallery-image-wrapper").first();
        if (div == null) {
            return null;
        }
        Element img = div.select("img.media-slideshow-image").first();
        if (img != null) {
            return img.absUrl("src");
        }
        return null;
    }

    private static String getImageFromLinkRel(Document document) {
        Element link = document.select("link[rel=image_src]").first();
        if (link != null) {
            return link.attr("abs:href");
        }
        return null;
    }

    private static String getImageFromTwitterCard(Document document) {
        Element meta = document.select("meta[name=twitter:card][content=photo]").first();
        if (meta == null) {
            return null;
        }
        Element image = document.select("meta[name=twitter:image]").first();
        return image.attr("abs:content");
    }

    private static String getImageFromOpenGraph(Document document) {
        Element image = document.select("meta[property=og:image]").first();
        if (image != null) {
            return image.attr("abs:content");
        }
        Element secureImage = document.select("meta[property=og:image:secure]").first();
        if (secureImage != null) {
            return secureImage.attr("abs:content");
        }
        return null;
    }

    private static String getImageFromSchema(Document document) {
        Element container =
                document.select("*[itemscope][itemtype=http://schema.org/ImageObject]").first();
        if (container == null) {
            return null;
        }

        Element image = container.select("img[itemprop=contentUrl]").first();
        if (image == null) {
            return null;
        }
        return image.absUrl("src");
    }
}