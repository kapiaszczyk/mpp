package dev.kapiaszczyk.mpp.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A type that represents one of two possible values.
 * <p>
 * Source of the base code: https://gist.github.com/makiftutuncu/aba3cfa627f90faa3a49579e400f81b9
 *
 * @param <L> The type of the left value.
 * @param <R> The type of the right value.
 */
public abstract class Either<L, R> {
    public static final class Left<L, R> extends Either<L, R> {
        public Left(L left) {
            super(left, null);
        }
    }

    public static final class Right<L, R> extends Either<L, R> {
        public Right(R right) {
            super(null, right);
        }
    }

    private final L left;
    private final R right;

    Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> ofLeft(L left) {
        return new Left<>(left);
    }

    public static <L, R> Either<L, R> ofRight(R right) {
        return new Right<>(right);
    }

    public static <L, R> Either<L, R> predicate(Supplier<Boolean> predicate, Supplier<L> left, Supplier<R> right) {
        return !predicate.get() ? ofLeft(left.get()) : ofRight(right.get());
    }

    public static <L, R> Either<L, R> predicate(boolean predicate, L left, R right) {
        return predicate(() -> predicate, () -> left, () -> right);
    }

    public Optional<L> left() {
        return Optional.ofNullable(left);
    }

    public Optional<R> right() {
        return Optional.ofNullable(right);
    }

    public boolean isLeft() {
        return left().isPresent();
    }

    public boolean isRight() {
        return right().isPresent();
    }

    public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> f) {
        if (isLeft()) {
            return Either.ofLeft(left);
        }

        return f.apply(right);
    }

    public <T> T fold(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        if (isLeft()) {
            return leftMapper.apply(left);
        } else {
            return rightMapper.apply(right);
        }
    }

    public <R2> Either<L, R2> map(Function<R, R2> f) {
        return flatMap(r -> Either.ofRight(f.apply(r)));
    }

    public void ifLeft(Consumer<L> f) {
        if (isLeft()) {
            f.accept(left);
        }
    }

    public void ifRight(Consumer<R> f) {
        if (isRight()) {
            f.accept(right);
        }
    }

    public L leftOrElse(L alternative) {
        return isLeft() ? left : alternative;
    }

    public R rightOrElse(R alternative) {
        return isRight() ? right : alternative;
    }
}
