package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PostRepositoryStubImpl implements PostRepository {
    private final AtomicLong newId = new AtomicLong();
    private final Set<Long> usedId = new CopyOnWriteArraySet<>();
    private final Set<Long> freeId = new CopyOnWriteArraySet<>();
    private final List<Post> posts = new CopyOnWriteArrayList<>();

    public List<Post> all() {
        return posts;
    }

    public Optional<Post> getById(long id) {
        for (Post post : posts) {
            if (post.getId() == id) {
                return Optional.of(post);
            }
        }
        return Optional.empty();
    }

    public Post save(Post post) {
        if (newId.get() == 0) {
            newId.set(1);
        }

        if (posts.size() == 0) {
            if (post.getId() == 0) {
                post.setId(newId.get());
                posts.add(post);
                usedId.add(newId.get());
                newId.incrementAndGet();
            } else {
                posts.add(post);
                usedId.add(post.getId());
            }
        } else {
            if (post.getId() == 0) {
                if (!freeId.isEmpty()) {
                    post.setId(freeId.stream().min(Comparator.naturalOrder()).get());
                    freeId.remove(post.getId());
                    posts.add(post);
                    usedId.add(post.getId());
                } else {
                    while (usedId.contains(newId.get())) {
                        newId.incrementAndGet();
                    }
                    post.setId(newId.get());
                    posts.add(post);
                    usedId.add(newId.get());
                }
            } else {
                if (usedId.contains(post.getId())) {
                    for (Post post1 : posts) {
                        if (post1.getId() == post.getId() && !post1.getContent().equals(post.getContent())) {
                            posts.remove(post1);
                            posts.add(post);
                        }
                    }
                } else {
                    posts.add(post);
                    usedId.add(post.getId());
                }
            }
        }
        return post;
    }

    public void removeById(long id) {
        if (usedId.contains(id)) {
            for (Post post1 : posts) {
                if (post1.getId() == id) {
                    posts.remove(post1);
                    usedId.remove(id);
                    freeId.add(id);
                }
            }
        } else {
            throw new NotFoundException();
        }
        if (posts.isEmpty()) {
            newId.set(1);
            freeId.clear();
        }
    }
}